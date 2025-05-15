# 1. 📌 목적

## 왜 View Table을 적용해야 하는가?

### 1-1 효율적인 데이터 처리
- 기존 문제: 기존 시스템에서는 주문 테이블에서 직접 상위 상품을 조회하는 쿼리가 복잡하고 성능에 부담을 주었습니다. 특히 상위 상품을 조회하는 작업이 자주 발생하면 데이터베이스 부하가 커져 성능 저하를 초래할 수 있습니다. 이러한 문제는 특히 트래픽이 많은 시스템에서 더욱 두드러지게 나타납니다.

- 해결책: 뷰 테이블(View Table)을 사용하여 상위 상품 정보를 미리 계산하고 저장함으로써, 조회할 때마다 복잡한 계산을 피하고 빠르게 결과를 반환할 수 있습니다. 뷰 테이블은 집계된 결과를 미리 저장해두므로 성능 최적화와 데이터베이스 부하 감소를 동시에 달성할 수 있습니다.

## 왜 Redis Sorted Set 활용해야 하는가?

### 1-2 빠르고 효율적인 데이터 조회

- Redis의 Sorted Set을 활용하면, 인기 상품과 같은 순위 기반 데이터를 실시간으로 업데이트하고 빠르게 조회할 수 있습니다. Sorted Set은 데이터를 점수(score)와 함께 저장하고, 점수에 따라 자동으로 정렬되므로 인기 상품을 효율적으로 관리할 수 있습니다. 또한, Redis는 메모리 기반 저장소이므로 높은 조회 성능을 제공합니다. 이 방식은 실시간 인기 상품 조회 시 효율적인 데이터 처리와 빠른 응답 시간을 보장합니다.

# 2. 🔍 문제 식별
### 2-1 인기 상품 조회
- 성능 저하: 주문 테이블에서 인기 상품을 계산하는 쿼리는 자주 실행될수록 성능에 부담을 주고, 데이터베이스 부하가 증가합니다.
  <br>기존 시스템에서는 복잡한 쿼리를 사용하여 주문 테이블에서 상위 상품 목록을 조회하고, 이 과정에서 여러 테이블 조인이나 집계 작업을 수행해야 했습니다. 이로 인해 불필요한 자원 소모가 발생하고, 응답 속도가 느려집니다.

# 3. ✅ 해결 방안

### 3-1 View Table 적용
#### 3-1-1 상위 상품 데이터를 위한 뷰 테이블 생성

```java
@Entity
@Table(name = "product_signal")
public class ProductSignal extends AuditableEntity {

    /**
     * 아이디
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_signal_id", nullable = false, updatable = false)
    private Long id;

    /**
     * 상품 ID
     */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /**
     * 날짜
     */
    @Column(name = "date", nullable = false)
    private LocalDate date;

    /**
     * 상품명
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * 주문 수량 (누계)
     */
    @Column(name = "order_count", nullable = false)
    private Long orderCount;
}
```

#### 3-1-2 상위 상품 데이터를 위한 쿼리 최적화 및 성능 개선

- 수정전: 복잡한 조인과 집계를 통한 상품 판매 데이터 조회
```java
    public List<ProductQuery.TopSelling> findTopSellingProducts(LocalDate daysAgo, long limit) {
        NumberExpression<Long> salesCount = orderItem.quantity.sum().castToNum(Long.class);
        LocalDateTime dateTime = LocalDateTime.of(daysAgo, LocalTime.MIN);

        return queryFactory
                .select(new QProductQuery_TopSelling(
                        product.id,
                        product.name,
                        salesCount
                ))
                .from(product)
                .join(productOption).on(product.id.eq(productOption.productId))
                .join(orderItem).on(productOption.id.eq(orderItem.optionId))
                .join(order).on(order.id.eq(orderItem.orderId))
                .where(
                        order.status.eq(Order.Status.SUCCESS),
                        order.orderDate.goe(dateTime)
                )
                .groupBy(product.id)
                .orderBy(salesCount.desc())
                .limit(limit)
                .fetch();
    }
```

- 수정후: 미리 집계된 데이터를 활용하여 성능 최적화
```java
    public List<ProductQuery.TopSelling> findTopSellingProducts(LocalDate daysAgo, long limit) {
        return queryFactory
                .select(new QProductQuery_TopSelling(
                        productSignal.productId.as("productId"),
                        productSignal.name,
                        productSignal.orderCount
                ))
                .from(productSignal)
                .where(productSignal.date.goe(daysAgo))
                .orderBy(productSignal.orderCount.desc())
                .limit(limit)
                .fetch();
    }
```

### 3-2 Redis Sorted Set 적용

- 기존
```java
public class ProductService {
    @CachePut(
            cacheNames = CACHE_NAME,
            key = "#daysAgo.toString().replace('-', '')"
    )
    public ProductInfo.ProductSalesData refreshTopSellingProductsCache(LocalDate daysAgo, long limit) {

        return new ProductInfo.ProductSalesData(productRepository.findTopSellingProducts(daysAgo, limit).stream()
                .map(ProductQuery.TopSelling::to)
                .toList());
    }
}
```
1. @CachePut 어노테이션을 사용하였습니다.
2. 캐시에서는 Object로 저장되며, 주기적으로 DB에서 데이터를 조회하여 캐시를 갱신하는 방식입니다.

- 수정 후
```java
@Repository
@RequiredArgsConstructor
public class ProductRedisRepositoryImpl implements ProductRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void addTopSellingProductToCache(long productId, Long orderCount) {
        String key = "top-selling:" + LocalDate.now().toString().replace("-", "");
        double score = orderCount; // 판매 수량을 score로 사용
        String value = String.valueOf(productId); // 상품 ID를 value로 사용

        // Redis Sorted Set에 데이터를 추가합니다.
        redisTemplate.opsForZSet().add(key, value, score);
    }
}
```
```java
public class ProductService {
    public void refreshTopSellingProductsCache(LocalDate daysAgo, long limit) {

        // 상품 판매 데이터를 DB에서 가져옵니다.
        List<ProductQuery.TopSelling> topSellingProducts = productRepository.findTopSellingProducts(daysAgo, limit);

        // Redis Sorted Set에 상품 데이터를 저장
        topSellingProducts.forEach(productSignal -> {
            productRedisRepository.addTopSellingProductToCache(productSignal.productId(), productSignal.salesCount());
        });
    }
}
```
1. Redis Sorted Set을 사용하여 판매 수량을 **score**로, 상품 ID를 **value**로 저장합니다.<br>이를 통해 상위 판매 상품을 효율적으로 관리가 가능합니다.

# 4. 📊 성능 테스트 및 결과 분석

#### 성능 테스트 도구로 k6를 사용하여, 두 가지 방법으로 성능 테스트를 진행합니다. 테스트의 목표는 기존 DB 조회와 View Table 조회 방식에 따른 성능 차이를 비교하는 것입니다.

### 4-1 테스트 케이스

#### 4-1-1 DB 조회
![Pasted Graphic 19](https://github.com/user-attachments/assets/ca4ceee7-5b47-457a-b49f-121c767dac64)

#### 성능 테스트 결과

- **테스트 시나리오**: 50명의 가상 사용자가 **30초 동안** Spike 테스트를 진행했습니다.
- **성공률**: 총 89번의 요청 중 23번만 성공 **(성공률 25.84%)**
- **실패율**: 66번의 요청이 실패 **(실패율 74.15%)** — Status is 200 실패
- **평균 응답 시간**: **10.46초**
- **최소 응답 시간**: **3.87초**
- **최대 응답 시간**: **15.9초**
- **90% 응답 시간 (p90)**: **14.6초**
- **95% 응답 시간 (p95)**: **14.9초**
- **처리량**: 초당 약 **2.25 요청/초** 처리


#### 4-1-2 View Table 조회
![Pasted Graphic 30](https://github.com/user-attachments/assets/d2555232-a8ad-4d2c-82a7-6d5b166a051d)

#### 성능 테스트 결과

- **테스트 시나리오**: 50명의 가상 사용자가 **30초 동안** Spike 테스트를 진행했습니다.
- **성공률**: 총 **3563번의 요청** 중 **100% 성공** (성공률 100%)
- **실패율**: 실패 요청 없음 (실패율 0%)
- **평균 응답 시간**: **183.67ms**
- **최소 응답 시간**: **21.45ms**
- **최대 응답 시간**: **730.16ms**
- **90% 응답 시간 (p90)**: **351.25ms**
- **95% 응답 시간 (p95)**: **376.16ms**
- **처리량**: 초당 약 **118.69 요청/초** 처리

### 4-2 성능 테스트 결과 비교

### 성능 테스트 결과 비교

항목 | DB 조회 (Test 1) | View Table 조회 (Test 2) | 향상률 (%)
-- | -- | -- | --
테스트 시나리오 | 50명의 가상 사용자가 30초 동안 Spike 테스트 진행 | 50명의 가상 사용자가 30초 동안 Spike 테스트 진행 | -
성공률 | 25.84% (23/89) | 100% (3563/3563) | 286.04%
실패율 | 74.15% (66/89) | 0% (0/3563) | -100%
평균 응답 시간 | 10.46초 | 183.67ms (0.18367초) | 99.43% 향상
최소 응답 시간 | 3.87초 | 21.45ms (0.02145초) | 99.44% 향상
최대 응답 시간 | 15.9초 | 730.16ms (0.73016초) | 98.08% 향상
90% 응답 시간 (p90) | 14.6초 | 351.25ms (0.35125초) | 98.55% 향상
95% 응답 시간 (p95) | 14.9초 | 376.16ms (0.37616초) | 98.45% 향상
처리량 | 초당 약 2.25 요청/초 처리 | 초당 약 118.69 요청/초 처리 | 5,174.22% 향상

# 6. 🏁 결론
- 이번 작업을 통해 Redis Sorted Set을 사용하여 상위 판매 상품 데이터를 효율적으로 관리하고, 판매 수량을 score로 사용하여 효율적인 조회가 가능해졌습니다.

- 뷰 테이블을 도입하여 상위 상품 정보를 미리 계산하고 저장함으로써 데이터베이스 부하를 줄이고, 성능을 개선할 수 있었습니다.

- Redis 캐시와 뷰 테이블을 결합하여, 빠른 응답 시간과 높은 처리량을 제공하며, 성능 최적화와 효율적인 데이터 관리를 가능했습니다.<br>
  특히 높은 트래픽을 처리하는 시스템에서 좋을 것 같습니다.




