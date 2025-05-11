# 1. 📌 목적

## 왜 Redis 캐시를 적용해야 하는가?

### 1-1 시스템 성능 향상
- 데이터베이스 조회 성능 저하: 사용자가 자주 조회하는 데이터(예: 인기 상품 목록)가 매번 데이터베이스에서 조회된다면, 데이터베이스 부하가 증가하고 성능 저하가 발생할 수 있습니다.
  <br>이러한 문제는 특히 높은 트래픽을 처리해야 하는 서비스에서 더욱 두드러지게 나타납니다

- Redis 캐시 적용: Redis를 메모리 내 캐시 시스템으로 사용하면, 자주 조회되는 데이터를 메모리 내에서 빠르게 처리할 수 있습니다.
  <br>데이터베이스를 직접 조회하지 않고 캐시에서 바로 데이터를 조회함으로써 성능을 최적화하고, 데이터베이스에 대한 부하를 현저히 줄일 수 있습니다.

### 1-2 캐시된 데이터 사용
- Redis는 키-값 구조로 데이터를 저장하기 때문에, 데이터를 빠르게 읽을 수 있습니다.

- 인기 상품 목록과 같은 자주 조회되는 데이터에 대해 캐시를 사용하면, 응답 속도를 대폭 향상시킬 수 있으며, 특히 동일한 데이터에 대한 반복적인 요청을 처리하는 데 유리합니다.

### 1-3 캐시 만료와 갱신
- 캐시된 데이터의 TTL(Time To Live)을 설정하여 일정 시간이 지나면 캐시가 자동으로 갱신되도록 할 수 있습니다.

- 이를 통해 최신 데이터를 제공하면서도, 데이터베이스에 대한 부담을 줄일 수 있습니다.

# 2. 🔍 문제 식별
### 2-1 인기 상품 조회
- 반복적인 조회: 인기 상품을 조회할 때마다 데이터베이스에 접근하는데, 이 작업이 자주 발생하며 성능 문제를 일으킬 수 있습니다.

- 해결책: Redis 캐시를 사용하여 자주 조회되는 인기 상품 데이터를 캐시에 저장하고, 캐시된 데이터를 조회하여 성능을 개선합니다.

# 3. 🧠 분석
### 3-1 기존 시스템 문제점
#### 3-1-1 느린 응답 속도
- 인기 상품을 조회할 때마다 데이터베이스에서 직접 조회해야 하므로 응답 속도가 느려집니다. 이로 인해 사용자가 상품 목록을 조회할 때 불필요하게 대기 시간을 겪게 되어 사용자 경험이 저하될 수 있습니다.

#### 3-1-2 데이터베이스 부하
- 인기 상품 조회 요청이 많을 경우, 데이터베이스 부하가 커져 성능 저하를 초래할 수 있습니다. 대량의 요청이 발생할 때마다 데이터베이스에 직접 쿼리를 보내는 것은 서버에 부담을 주고, 동시에 여러 요청이 처리되는 경우 데이터베이스 성능이 급격히 떨어질 수 있습니다.

### 3-2 캐시 적합한 데이터
#### 3-2-1 인기 상품 정보
- 인기 상품 정보는 자주 조회되는 데이터입니다. 이 데이터는 변경이 빈번하지 않지만 반복적으로 조회됩니다. 따라서 캐시에 적합한 데이터로, 데이터베이스에서 직접 조회하는 대신 Redis와 같은 캐시 시스템에 데이터를 저장하고 이를 사용하면 빠른 응답을 제공할 수 있습니다.

- 캐시 시스템을 사용함으로써, 사용자가 상품 목록을 요청할 때 데이터베이스 부하를 줄이고, 캐시된 데이터를 이용하여 훨씬 빠르게 응답할 수 있습니다.

# 4. ✅ 해결 방안

### 4-1 Redis 캐시 적용
#### 4-1-1 Read Through 전략

---

- Cache Hit 의 경우

![image](https://github.com/user-attachments/assets/a850a5a7-fc07-470a-8ac7-baefb69f8c1f)

1. 사용자가 인기 상품을 요청합니다.

2. Redis에서 캐시된 데이터를 조회합니다.

3. Redis에 데이터가 있으면 캐시에서 바로 사용자에게 반환합니다.

4. 사용자에게 조회된 데이터를 반환합니다.

---

- Cache Miss 의 경우

![image](https://github.com/user-attachments/assets/210123bd-14b4-47a7-b1a8-fa7f808172c0)

1. 사용자가 인기 상품을 요청합니다.

2. Redis에서 캐시된 데이터를 조회합니다.

3. Cache Miss가 발생하면, DB에서 데이터를 조회합니다.

4. DB에서 데이터를 조회하고 Redis에 저장합니다.

5. 사용자에게 조회된 데이터를 반환합니다.

6. 다음 요청에 대해 Redis에서 바로 캐시 데이터를 조회합니다.

---
```java
@Service
public class ProductService {
    // 인기 상품 조회
    @Cacheable(
            cacheNames = CACHE_NAME,
            key = "#daysAgo.toString().replace('-', '')",
            unless = "#result == null or #result.list == null or #result.list.empty"
    )
    @Transactional(readOnly = true)
    public ProductInfo.ProductSalesData getTopSellingProducts(LocalDate daysAgo, long limit) {
        List<ProductInfo.TopSelling> topSellings = productRepository.findTopSellingProducts(daysAgo, limit).stream()
                .map(ProductQuery.TopSelling::to)
                .toList();

        return ProductInfo.ProductSalesData.builder()
                .list(topSellings)
                .build();
    }
}
```
- 단점 : Read-Through 전략의 단점은 주로 캐시 스탬피드(Cache Stampede)와 관련이 있습니다. 캐시 미스가 발생하면 여러 사용자 요청이 동시에 DB로 몰려 DB 부하와 성능 저하를 초래할 수 있습니다. 또한, 캐시 갱신 시 데이터 일관성 문제가 발생할 수 있습니다.

---

#### 4-1-2 Write Through 전략

![image](https://github.com/user-attachments/assets/48432ac0-d670-4585-b6d6-5e4c1e5b48e9)

1. 스케줄러가 주기적으로 실행되어 데이터를 갱신합니다.

2. DB에서 데이터를 조회하거나 업데이트합니다.

3. Redis 캐시를 갱신하여, DB와 캐시가 동기화됩니다.

--- 
```java
@Component
public class ProductScheduler {

    private final ProductService productService;

    @Scheduled(cron = "0 30 23 * * *") // 매일 23:30 실행
    public void refreshTopSellingProductsCache() {
        LocalDate daysAgo = LocalDate.now().minusDays(3);
        long limit = 5;

        productService.refreshTopSellingProductsCache(daysAgo, limit);
    }

}
```
```java
@Service
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
사용 이유
- 매일 23시 30분에 갱신되는 인기 상품 데이터를 사용자에게 제공할 때, 사용자가 직접 DB를 조회하지 않고 캐시된 데이터만으로 조회할 수 있도록 하여 성능 최적화와 데이터 일관성 유지가 가능하기 때문입니다.

---

### 4-2 TTL 설정

![image](https://github.com/user-attachments/assets/92557d3e-513c-4147-8bae-5a28b8dc1518)

TTL 설정 (25시간):

- 인기 상품 정보는 자주 변경되지 않기 때문에, TTL을 25시간으로 설정하여 캐시 만료와 갱신 주기를 다르게 구성했습니다. <br>이를 통해 캐시된 데이터만을 조회할 수 있게 하여, 사용자는 DB 조회 없이 빠르게 캐시된 데이터를 조회 할수 있습니다.

# 5. 📊 성능 테스트 및 결과 분석

#### 성능 테스트 도구로 k6를 사용하여, 두 가지 방법으로 성능 테스트를 진행합니다. 테스트의 목표는 DB 조회와 Redis 캐시 조회 방식에 따른 성능 차이를 비교하는 것입니다.

### 5-1 테스트 케이스

#### 5-1-1 DB 조회
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


#### 5-1-2 Redis 캐시 조회
![Pasted Graphic 18](https://github.com/user-attachments/assets/a7eef729-04f9-4cf9-a8c2-c1cb7c35bf17)

#### 성능 테스트 결과

- **테스트 시나리오**: 50명의 가상 사용자가 **30초 동안** Spike 테스트를 진행했습니다.
- **성공률**: 총 **28,110번의 요청** 중 **100% 성공** (성공률 100%)
- **실패율**: 실패 요청 없음 (실패율 0%)
- **평균 응답 시간**: **23.1ms**
- **최소 응답 시간**: **1.99ms**
- **최대 응답 시간**: **195.28ms**
- **90% 응답 시간 (p90)**: **43.7ms**
- **95% 응답 시간 (p95)**: **50.69ms**
- **처리량**: 초당 약 **936.95 요청/초** 처리

### 5-2 성능 테스트 결과 비교

### 성능 테스트 결과 비교

| 항목                           | **DB 조회 (Test 1)**           | **Redis 캐시 조회 (Test 2)** | 향상률 (%)  |
|--------------------------------|--------------------------------|------------------------------|------------|
| **테스트 시나리오**             | 50명의 가상 사용자가 30초 동안 Spike 테스트 진행 | 50명의 가상 사용자가 30초 동안 Spike 테스트 진행 | -          |
| **성공률**                      | 25.84% (23/89)                | 100% (28,110/28,110)         | 100%       |
| **실패율**                      | 74.15% (66/89)                | 0% (0/28,110)                | -100%      |
| **평균 응답 시간**              | 10.46초                       | 23.1ms (0.0231초)            | 99.78% 향상 |
| **최소 응답 시간**              | 3.87초                        | 1.99ms (0.00199초)           | 99.95% 향상 |
| **최대 응답 시간**              | 15.9초                        | 195.28ms (0.19528초)         | 98.02% 향상 |
| **90% 응답 시간 (p90)**         | 14.6초                        | 43.7ms (0.0437초)            | 99.70% 향상 |
| **95% 응답 시간 (p95)**         | 14.9초                        | 50.69ms (0.05069초)          | 99.66% 향상 |
| **처리량**                      | 초당 약 2.25 요청/초 처리      | 초당 약 936.95 요청/초 처리   | 41,542.22% 향상 |


# 6. ⚠️ 한계점 및 고민
### 6-1 캐시 일관성 문제
- 캐시와 데이터베이스의 데이터가 일치하지 않을 수 있는 문제가 발생할 수 있습니다. 이를 해결하기 위해 캐시 무효화 전략이나 캐시 갱신 정책을 명확히 정의해야 합니다.

### 6-2 Redis 장애 시 대비책
- Redis 장애나 연결 문제가 발생하면 캐시된 데이터를 사용할 수 없게 되므로, 캐시 장애에 대비해 적절한 fallback 전략을 마련해야 합니다. 예를 들어, Redis가 다운될 경우 데이터베이스에서 직접 조회하는 방식으로 fallback할 수 있습니다.

# 7. 🏁 결론
- Redis 캐시를 활용하여 인기 상품 조회 성능을 최적화하고, 데이터베이스의 부하를 줄일 수 있었습니다.

- 캐시 갱신 및 TTL 설정을 통해, 최신 데이터를 사용자에게 제공하면서도 성능을 크게 향상시킬 수 있었습니다.

- 향후 캐시 일관성 문제를 해결하기 위한 정책과 Redis 장애 대비책을 추가로 고민할 필요가 있습니다.