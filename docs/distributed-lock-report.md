# 1. 📌 목적

## 왜 분산락을 적용해야 하는가?

### 1-1 시스템의 동시성 이슈
- 여러 클라이언트가 동시에 동일한 자원에 접근하려 할 때, 경쟁 조건(race conditions)이나 데이터 불일치가 발생할 수 있습니다.
- 예를 들어, 재고 차감이나 쿠폰 발급과 같은 작업에서는 동일한 데이터를 여러 클라이언트가 동시에 수정하려 할 수 있습니다.
- 이런 문제를 해결하기 위해 분산락을 적용하여, 하나의 클라이언트만 자원에 접근하도록 보장해야 합니다.

### 1-2 성능 이슈
- 동시성 문제를 해결하지 않으면, 트랜잭션 충돌이나 비효율적인 데이터 처리가 발생하여 성능 저하를 초래할 수 있습니다.
- 락을 제대로 관리하지 않으면 성능 문제로 이어질 수 있습니다.

### 1-3 비관적 락(Pessimistic Lock)만으로는 충분하지 않음
- 비관적 락은 데이터베이스수준에서만 작동하므로, 여러 서버에서 분산된 데이터를 처리하는 수평 확장 환경에서는 효율적이지 않거나 확장성이 부족할 수 있습니다.

### 1-4 분산락 종류 비교

분산락 종류 | 설명 | 특징 | 사용 사례
-- | -- | -- | --
심플락 (Simple Lock) | 하나의 자원에 대해 락을 걸고 해제하는 가장 기본적인 형태의 분산락 | - 자원에 대해 하나의 락만 걸고 해제함- 비교적 간단하고 효율적 | - 재고 차감, 단일 자원에 대한 락을 걸 때
스핀락 (Spin Lock) | 락을 획득할 때, 락을 얻을 때까지 계속해서 "대기"하는 방식 | - 락을 얻을 때까지 계속해서 시도하며 대기- CPU를 계속 사용하므로 자원을 많이 소모할 수 있음- 짧은 시간 동안 락을 걸 때 효율적 | - 빠른 락 획득이 필요할 때 (예: 작은 자원에 대한 락)- 자주 업데이트되지 않는 자원에 대한 락을 사용할 때
펍섭락 (Pub/Sub Lock) | Redis의 Pub/Sub 기능을 활용하여, 락 획득 여부를 다른 클라이언트와 실시간으로 알리는 방식 | - 분산 환경에서 여러 클라이언트가 락 획득 여부를 알 수 있게 함- 높은 동시성 환경에서 유리함- Redis Pub/Sub를 사용 | - 다수의 클라이언트가 락을 기다리고 있는 환경에서 락 획득 상태를 실시간으로 알려줄 때 사용

# 2. 🔍 문제 식별

## 어떤 공유 자원에서 동시성 문제가 발생하는지

### 2-1 재고 차감
- 여러 주문이 동시에 처리될 때, 동일한 상품 옵션에 대한 재고가 중복으로 차감될 위험이 있습니다. 이는 동시성 문제로 인해 재고가 잘못 처리될 수 있는 상황입니다.

### 2-2 선착순 쿠폰 발급
- 여러 사용자가 동시에 동일한 쿠폰을 요청하면, 중복 발급이 발생할 수 있습니다. 선착순으로 한정된 쿠폰 수를 처리할 때 이러한 문제가 발생할 수 있습니다.

# 3. 🧠 분석

### 3-1 재고
- 여러 주문이 동시에 들어오면 재고 차감에서 충돌이 발생할 수 있습니다. 비관적 락이 적용되어 있지만, 분산락을 적용하면 여러 서버 환경에서도 안정적으로 재고를 관리할 수 있습니다.

### 3-2 쿠폰
-쿠폰 발급 시, 여러 사용자가 동시에 발급 요청을 하면 중복 발급이 발생할 수 있습니다. 이 역시 비관적 락을 사용하여 일부 동시성 문제를 해결하고 있지만, 분산락을 적용하면 보다 효율적으로 중복 발급을 방지할 수 있습니다.

# 4. ✅ 해결 방안

### 4-1 분산락 적용

#### 4-1-1 AOP 방식
- AOP 방식을 사용하여 분산락을 적용할 수 있습니다. `@DistributedLock` 애노테이션을 메서드에 적용하고, AOP에서 해당 메서드를 가로채어 락을 처리합니다. 이 방식은 각각의 메서드 호출에 대해 락을 자동으로 적용하고 해제할 수 있어, 코드의 중복을 최소화하고 선언적으로 락을 처리할 수 있는 장점이 있습니다.

```java
    @Transactional
    @DistributedLock(
            topic = "stock",
            keyExpression = "#criteria.toOptionIds()",
            waitTime = 5,
            leaseTime = 3
    )
    public OrderResult.OrderSummary order(OrderCriteria.Detail criteria) {}
```

- @Order(Ordered.HIGHEST_PRECEDENCE)를 사용하여 AOP가 가장 먼저 실행되도록 합니다. 이렇게 함으로써, 락 획득이 트랜잭션 처리 전에 먼저 수행되며, 트랜잭션 내에서 락을 안전하게 관리할 수 있습니다.

```java
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RedissonLockAspect {}
```

#### 4-1-2 SpEL 방식
- SpEL(Spring Expression Language)을 사용하여 락의 키를 동적으로 계산할 수 있습니다. 예를 들어, 주문을 처리할 때 `#criteria.toOptionIds()`와 같은 표현식을 사용하여 주문에 대한 옵션 ID 리스트를 기반으로 락을 걸 수 있습니다.

```java
public final class OrderCriteria {

    @Getter
    @Builder
    public static class Detail {
        private long userId;
        private List<Item> items;

        public List<Long> toOptionIds() {
            return this.items.stream().map(Item::getOptionId).sorted().toList();
        }
    }
}
```

- SpEL 코드 설명:
  - `SpelExpressionParser`를 사용하여 keyExpression(예: `#criteria.toOptionIds()`)을 파싱하고, 해당 표현식에서 동적으로 옵션 ID 리스트를 추출하여 락 키로 사용합니다.
  - StandardEvaluationContext를 이용해 메서드 인자 값을 컨텍스트에 바인딩하고, `expression.getValue()`를 사용하여 실제 값을 계산합니다.

```java
SpelExpressionParser parser = new SpelExpressionParser();
Expression expression = parser.parseExpression(keyExpression);

StandardEvaluationContext context = new StandardEvaluationContext();
context.setVariable(pair.getFirst().getName(), pair.getSecond());
Object idObject = expression.getValue(context);
```

- `keyExpression`을 통해 동적으로 값을 계산하고, 이를 락 키로 사용하여 동시성 문제를 해결합니다.

### 4-2 재고 차감
#### 4-2-1 키 설계
- 재고 차감 시, productOptionId를 사용하여 락을 설정합니다. 이는 각각의 상품 옵션에 대해 락을 걸어, 동시 재고 차감을 방지하는 데 유용합니다.

#### 4-2-2 범위 설정 기준
- productOptionId를 기준으로 락을 설정하여, 동시 재고 차감이 발생하지 않도록 합니다. 여러 주문이 동시에 들어올 경우에도 각 상품 옵션에 대한 락을 관리하여, 충돌을 방지합니다.

#### 4-2-3 파사드 외부에 락 적용
- 주문 파사드에 트랜잭션이 걸려있어, 재고 차감 서비스가 파사드의 트랜잭션 내에서 처리되고 있습니다. 이를 해결하기 위해 락을 파사드 외부에서 적용합니다.
  분산락을 파사드 트랜잭션 외부에서 적용하여, 재고 차감 로직을 트랜잭션과 독립적으로 처리할 수 있습니다.

### 4-3 쿠폰 발급
#### 4-3-1 키 설계
- couponId와 같은 고유한 식별자를 사용하여 락을 설정합니다. 이렇게 함으로써 동일한 쿠폰의 중복 발급을 방지할 수 있습니다.

#### 4-3-2 범위 설정 기준
- couponId를 기준으로 락을 설정하여 중복 발급을 방지합니다. 여러 사용자가 동시에 동일한 쿠폰을 요청할 때 락을 걸어, 중복 발급이 발생하지 않도록 합니다.

### 4-4 멀티락 사용
- 여러 상품 옵션을 동시에 처리해야 하는 주문 API에서 멀티락을 사용하여, 각 상품 옵션에 대해 락을 동시에 관리할 수 있습니다.
- getMultiLock() 메서드를 사용하여 여러 자원에 대해 락을 한번에 관리하고, 동시성 문제를 해결합니다.

```java
        RLock[] locks = ids.stream()
                .map(id -> "%s:%s".formatted(topic, id))
                .map(redissonClient::getLock)
                .toArray(RLock[]::new);

        RLock multiLock = redissonClient.getMultiLock(locks);

        try {
            long waitTime = annotation.waitTime();
            long leaseTime = annotation.leaseTime();
            TimeUnit unit = annotation.unit();

            available = multiLock.tryLock(waitTime, leaseTime, unit);

            if (!available) {
                throw new IllegalStateException("락 획득 실패 - ids: " + ids);
            }

            return joinPoint.proceed();
        } finally {
            if (available) {
                multiLock.unlock();
            }
        }

```

- 멀티락 vs 반복문을 통한 락

항목 | 멀티락 (getMultiLock()) | 반복문을 통한 락
-- | -- | --
락 획득 방식 | 여러 락을 동시에 처리 | 각 락을 개별적으로 처리
성능 | 더 효율적이며 성능상 우위 | 여러 번의 락 시도와 해제를 통해 성능이 떨어질 수 있음
유연성 | 모든 락을 한 번에 처리하고 실패 시 모든 락을 해제함 | 각 락에 대해 별도의 처리 가능
관리의 용이성 | 코드가 간결하고 유지보수 용이 | 여러 락을 개별적으로 관리해야 하므로 코드가 복잡해짐
부분 해제 가능 | 불가능. 모든 락을 한 번에 해제 | 가능. 실패한 락에 대해 개별적으로 처리 가능

# 5. ⚠️ 한계점 및 고민
### 5-1 Redis 장애 시 대비책 부재
- Redis 장애나 연결 문제가 발생할 경우, 분산락이 동작하지 않거나 예기치 못한 문제가 발생할 수 있습니다. 예를 들어, Redis 서버가 다운되거나 네트워크 문제로 인해 분산락을 획득할 수 없게 되는 상황이 발생할 수 있습니다.

- 해결책 - 비관적 락(Pessimistic Lock)을 사용하여, Redis 장애가 발생해도 비관적 락으로 동시성 문제를 해결할 수 있는 방법을 고려할 수 있습니다. 비관적 락은 데이터베이스에서 락을 걸기 때문에, Redis가 동작하지 않더라도 DB에서 락을 계속 유지할 수 있습니다. 이렇게 하면 Redis의 장애로 인한 락 획득 실패 시에도 데이터베이스 레벨에서 안정적인 동시성 제어가 가능합니다.

# 6. 🏁 결론
- 분산락을 적용하여 동시성 문제를 해결하고, 데이터 불일치를 방지할 수 있었습니다. Redisson을 사용하여 높은 성능과 확장성을 제공하는 분산락을 구현하고, 적절한 키 설계를 통해 자원에 대한 락을 효과적으로 관리할 수 있었습니다.