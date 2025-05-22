# 1. 📌 목적

## 왜 Redis 기반 선착순 쿠폰 발급 시스템을 적용해야 하는가?

### 1-1 시스템 성능 향상
- 기존 시스템의 문제점: 선착순 쿠폰 발급 시스템에서 발급 대기 유저들을 데이터베이스에서 처리하면, 발급 대기 큐와 발급 상태를 매번 데이터베이스에서 조회해야 하므로 성능 저하와 높은 데이터베이스 부하가 발생할 수 있습니다.
  <br>특히 동시성 문제와 트래픽이 많은 서비스에서 이러한 문제는 더욱 심각해집니다.

- Redis 기반 시스템 적용: Redis를 메모리 기반의 캐시 시스템으로 사용하여, 발급 대기 큐와 발급 상태를 빠르게 처리할 수 있습니다.
  <br>Redis에서 발급 대기 유저 리스트와 발급 상태 관리를 처리함으로써, 데이터베이스 부하를 줄이고, 발급 속도를 크게 향상시킬 수 있습니다.

### 1-2 시스템 확장성
- Redis 활용으로 확장성 제공: Redis는 높은 처리 성능을 바탕으로 수많은 유저를 처리할 수 있습니다. 또한, 분산 시스템을 통해 수평 확장을 지원하며, 발급 대기 큐와 유저 관리를 효율적으로 처리할 수 있습니다.

# 2. ✅ 해결 방안

### 2-1 선착순 쿠폰 발급 요청

- 자료구조: 쿠폰 발급 대기 큐를 관리하기 위해 Redis의 Sorted Set 자료구조를 사용합니다.

- Key: coupon:{couponId}:issued

- Value: 유저 ID (쿠폰을 발급받으려는 유저의 ID)

- Score: 발급 요청 시점의 타임스탬프로, Redis Sorted Set에서 점수(score)로 사용됩니다. 이렇게 함으로써, 가장 먼저 요청한 유저부터 발급할 수 있습니다.

![image](https://github.com/user-attachments/assets/9615652f-2a57-4681-9bc0-27d303420645)
1. 발급 요청: 사용자가 쿠폰을 발급받기 위해 시스템에 요청을 보냅니다.

2. Redis에 요청 추가: 유저의 쿠폰 요청은 Redis의 Sorted Set에 타임스탬프를 점수로 사용하여 선착순으로 저장됩니다. 이때 유저 ID가 값(value)으로 저장되어 발급 순서를 관리합니다.

```java
@Repository
@RequiredArgsConstructor
public class CouponRedisRepositoryImpl implements CouponRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public Boolean addCouponRequestToQueue(Long userId, Long couponId) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        // 발급 요청 시점 (현재 타임스탬프)을 점수로 사용
        double score = System.currentTimeMillis();  // 현재 시각을 점수로 사용하여 선착순 처리

        // 쿠폰 발급 요청을 SortedSet에 추가
        return zSetOps.addIfAbsent("coupon:" + couponId + ":issued", String.valueOf(userId), score);
    }
}
```
- zSetOps.add는 중복 값을 허용하고 점수만 업데이트하므로, 값 자체를 변경할 필요가 있을 때 사용합니다.
- zSetOps.addIfAbsent는 중복 값을 방지하고, 값이 존재하지 않을 때만 추가하므로 중복 처리가 필요한 경우에 사용합니다.

### 2-2 유저 선착순 쿠폰 발급

![image](https://github.com/user-attachments/assets/686a2718-b047-4181-8ce2-09f93eabdd38)
1. 스케줄러는 주기적으로 쿠폰 발급 요청합니다.
2. Redis는 유저를 선착순으로 관리하고, 발급 대기 큐에서 유저를 처리합니다.
3.  DB는 발급된 쿠폰 상태와 유저 쿠폰 정보를 저장하여, 영속성을 보장하고 중복 발급을 방지합니다.

```java
    @Scheduled(fixedRate = 10000) // 10초마다 실행
    public void issueCoupons() {
        couponFacade.issuedCoupon();
    }
```
```java
    @Entity
    @Table(name = "coupon")
    public class Coupon extends AuditableEntity {
      /**
       * 쿠폰 상태
       */
      @Enumerated(EnumType.STRING)
      @Column(name = "status", nullable = false)
      private Coupon.Status status;

      public enum Status {
          STANDARD,
          AVAILABLE,    // 발급 가능
          ISSUED,       // 발급 완료
          EXPIRED       // 만료됨
      }
    }


    @Transactional
    public void issuedCoupon() {
        // 1. 발급 가능한 상태인 쿠폰들을 조회합니다.
        List<CouponInfo.AvailableCoupon> byCouponStatus = couponService.findByCouponStatus();

        // 2. 발급 가능한 쿠폰들에 대해 반복 처리를 시작합니다.
        byCouponStatus.
                stream()
                .map(v -> CouponCommand.Issued.of(v.getCouponId(), v.getQuantity(), v.getStatus()))
                .forEach(couponService::issuedCoupon);
    }
```
- 기존 방식: 이전에는 쿠폰 개수를 기준으로 발급 가능 여부를 판단하고, 쿠폰이 발급될 때마다 쿠폰 개수를 차감하는 방식이었습니다.<br>이 방식은 쿠폰 개수가 0이 될 때까지 발급이 가능했으므로, 쿠폰의 재고를 관리하는 데 한계가 있었습니다.<br>또한, 중간에 오류가 발생하거나, 발급 상태가 관리되지 않으면, 이미 발급된 쿠폰에 대해 중복 발급이나 잘못된 재고 관리가 발생할 수 있었습니다.

- 변경된 방식: 쿠폰 상태를 추가하여 발급 가능 상태인 쿠폰에 대해서만 발급이 이루어지도록 보호 기능을 강화하였습니다.<br>쿠폰 상태를 AVAILABLE로 설정하고, ISSUED나 EXPIRED 상태인 쿠폰은 발급할 수 없도록 하여, 발급 가능한 쿠폰만을 처리하도록 했습니다.<br>이를 통해, 발급 상태에 따른 불필요한 발급을 방지하고, 쿠폰 재고와 발급 상태를 보다 명확하고 안전하게 관리할 수 있게 되었습니다.

# 3. 📊 테스트 및 결과 분석

### 3-1 선착순 쿠폰 발급 요청
```java
        @Test
        @DisplayName("유저 선착순 쿠폰 발급 테스트")
        void addCouponToQueue() {

            // when
            CouponInfo.CouponActivation couponToQueue = sut.addCouponToQueue(issuedCoupon);


            // then
            Set<String> couponRequestQueue =
                    couponRedisRepository.getCouponRequestQueue(coupon.getId(), coupon.getQuantity());

            assertThat(couponRequestQueue).hasSize(1);
            assertThat(couponRequestQueue).contains(String.valueOf(couponToQueue.getUserId()));

        }
```
- 통합테스트가 더 효과적인 테스트라고 판단 하여 통합테스트로 진행 하였습니다.
- Redis에 발급 대기 큐가 정확히 동작하는지 확인합니다.
- 유저 ID가 Redis 대기 큐에 정확히 저장되었으며, 발급 대기 큐의 중복 방지가 효과적으로 동작하고 있음을 확인할 수 있었습니다.

### 3-2 유저 선착순 쿠폰 발급
```java
        @Test
        @DisplayName("선착순 쿠폰 발급 완료 테스트")
        void success1() {

            // when
            for (int i = 0; i < 2; i++) {
                couponScheduler.issueCoupons();
            }

            // then
            List<UserCoupon> userCoupons = couponRepository.findUserCouponsById(List.of(coupon.getId()));

            Optional<Coupon> couponById = couponRepository.findCouponById(coupon.getId());

            assertThat(userCoupons).hasSize(1);
            assertThat(userCoupons.get(0).getId()).isEqualTo(coupon.getId());
            assertThat(couponById.get().getStatus()).isEqualTo(Coupon.Status.ISSUED);

        }
```
- 통합테스트가 더 효과적인 테스트라고 판단 하여 통합테스트로 진행 하였습니다.
- 반복문 내에서 2번 호출하는 이유는, issueCoupons() 메서드를 두 번 호출하여 쿠폰 발급이 잘 진행되는지 그리고 상태가 제대로 변경되는지 테스트하려는 것입니다.
- 테스트 성공: 이 테스트는 couponScheduler.issueCoupons() 메서드를 두 번 호출했음에도 불구하고, 쿠폰 발급이 한 번만 이루어졌고, 상태가 ISSUED로 정확히 변경되었습니다.
- 상태 변경: ISSUED 상태로의 정확한 변경은 쿠폰 발급 로직이 로직이 잘 적용되었음을 나타냅니다.

# 4. ⚠️ 한계점 및 고민
### 4-1 유저 선착순 쿠폰 발급
#### 4-1-1 동시성 처리의 복잡성
- 선착순 쿠폰 발급 시스템에서 여러 유저가 동시에 쿠폰을 발급 요청할 때, 중복 발급을 방지하는 로직이 복잡할 수 있습니다. 특히, Redis와 데이터베이스의 동기화가 제대로 이루어지지 않으면, 경쟁 조건 (race condition)이 발생할 수 있습니다.

#### 4-1-2 Redis와 데이터베이스 간의 일관성
- Redis를 발급 대기 큐와 상태 관리에 사용하고 있지만, Redis와 데이터베이스 간의 데이터 일관성을 어떻게 관리할지에 대한 고민이 필요합니다.<br>예를 들어, Redis에서 유저가 대기 큐에 추가된 상태에서, DB에서 상태가 제대로 갱신되지 않으면 데이터 불일치가 발생할 수 있습니다.<br>이를 해결하기 위해 트랜잭션 관리와 동기화 전략을 신중히 설계해야 합니다.

# 5. 🏁 결론
- Redis Sorted Set을 활용해 발급 대기 큐를 효율적으로 관리하고, 쿠폰 상태를 통해 발급 가능 여부를 명확히 하여 성능을 최적화하고 데이터베이스 부하를 줄였습니다.<br>
  앞으로는 동시성 처리와 서비스 확장성 문제를 개선하고, 예외 처리 및 발급 로직을 개선하여 시스템의 안정성을 더욱 강화할 필요가 있습니다.
- 카프카 이벤트를 사용하면 비동기 처리로 성능과 확장성을 개선하고, 분산 시스템에서의 일관성 유지와 유연한 확장이 가능해질거 같습니다.

