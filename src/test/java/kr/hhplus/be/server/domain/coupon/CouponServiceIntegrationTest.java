package kr.hhplus.be.server.domain.coupon;

import jakarta.persistence.EntityManager;
import kr.hhplus.be.server.common.exception.BusinessError;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.test.util.RandomGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
@DisplayName("쿠폰서비스 통합 테스트")
class CouponServiceIntegrationTest {

    @Autowired
    CouponService sut;

    @Autowired
    private EntityManager entityManager;

    @Nested
    @DisplayName("issueCoupon 테스트")
    class IssueCouponTest {

        @Test
        @DisplayName("쿠폰 발급 테스트")
        void success() {
            // given
            User user = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(User.class)
                    .set("id", null)
                    .build()
                    .sample();
            entityManager.persist(user);

            long couponQuantity = 10;

            Coupon coupon = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Coupon.class)
                    .set("id", null)
                    .set("quantity", couponQuantity)
                    .build()
                    .sample();
            entityManager.persist(coupon);

            CouponCommand.IssuedCoupon command = new CouponCommand.IssuedCoupon(user.getId(), coupon.getId());

            // when
            CouponInfo.IssuedCoupon result = sut.issueCoupon(command);

            // then
            List<CouponInfo.OwnedCoupon> userCoupons = sut.findUserCoupons(user.getId())
                    .stream()
                    .filter(v -> v.getCouponId() == coupon.getId()).toList();

            assertThat(result).isNotNull();
            assertThat(result.getCouponId()).isEqualTo(coupon.getId());
            assertThat(result.getCouponName()).isNotBlank();
            assertThat(userCoupons.get(0).getCouponId()).isEqualTo(result.getCouponId());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 쿠폰")
        void failure1() {
            // given
            long userId = RandomGenerator.nextPositiveLong(Long.MAX_VALUE);
            long invalidCouponId = RandomGenerator.nextPositiveLong(Long.MAX_VALUE);
            CouponCommand.IssuedCoupon command = new CouponCommand.IssuedCoupon(userId, invalidCouponId);

            // expect
            assertThatThrownBy(() -> sut.issueCoupon(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessError.COUPON_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("실패 - 쿠폰 수량 부족")
        void failure2() {
            // given
            User user = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(User.class)
                    .set("id", null)
                    .build()
                    .sample();
            entityManager.persist(user);

            long couponQuantity = 0;

            Coupon coupon = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Coupon.class)
                    .set("id", null)
                    .set("quantity", couponQuantity)
                    .build()
                    .sample();
            entityManager.persist(coupon);

            CouponCommand.IssuedCoupon command = new CouponCommand.IssuedCoupon(user.getId(), coupon.getId());

            // expect
            assertThatThrownBy(() -> sut.issueCoupon(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessError.COUPON_ISSUE_LIMIT_EXCEEDED.getMessage());
        }

        @Test
        @DisplayName("실패 - 이미 발급된 쿠폰")
        void failure3() {
            // given
            User user = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(User.class)
                    .set("id", null)
                    .build()
                    .sample();
            entityManager.persist(user);

            Coupon coupon = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Coupon.class)
                    .set("id", null)
                    .set("quantity", RandomGenerator.nextLong(1, 10))
                    .build()
                    .sample();
            entityManager.persist(coupon);

            UserCoupon userCoupon = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(UserCoupon.class)
                    .set("id", null)
                    .set("couponId", coupon.getId())
                    .set("userId", user.getId())
                    .set("used", true)
                    .build()
                    .sample();
            entityManager.persist(userCoupon);

            // expect
            assertThatThrownBy(() -> sut.issueCoupon(new CouponCommand.IssuedCoupon(user.getId(), coupon.getId())))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessError.COUPON_ALREADY_ISSUED.getMessage());
        }
    }

    @Nested
    @DisplayName("use 테스트")
    class UseTest {

        @Test
        @DisplayName("쿠폰 사용 성공 테스트")
        void success1() {

            // given
            User user = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(User.class)
                    .set("id", null)
                    .build()
                    .sample();
            entityManager.persist(user);

            LocalDateTime startedDate = LocalDateTime.now().minusDays(3);
            LocalDateTime endedDate = LocalDateTime.now().plusDays(3);

            Coupon coupon = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Coupon.class)
                    .set("id", null)
                    .set("quantity", RandomGenerator.nextLong(1, 10))
                    .set("startedDate", startedDate)
                    .set("endedDate", endedDate)
                    .build()
                    .sample();
            entityManager.persist(coupon);

            UserCoupon userCoupon = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(UserCoupon.class)
                    .set("id", null)
                    .set("couponId", coupon.getId())
                    .set("userId", user.getId())
                    .set("used", false)
                    .build()
                    .sample();
            entityManager.persist(userCoupon);

            List<Long> userCouponIds = List.of(userCoupon.getId());
            CouponCommand.Use command = CouponCommand.Use.of(userCouponIds);

            // when
            sut.use(command);

            // then
            CouponCommand.FindUserCoupons findUserCoupons = CouponCommand.FindUserCoupons.of(userCouponIds);
            List<CouponInfo.OwnedCoupon> userCouponsById = sut.findUserCouponsById(findUserCoupons);
            CouponInfo.OwnedCoupon ownedCoupon = userCouponsById.get(0);

            assertThat(userCouponsById).hasSize(1);
            assertThat(ownedCoupon.getCouponId()).isEqualTo(coupon.getId());
            assertThat(ownedCoupon.isUsed()).isTrue();
        }

        @Test
        @DisplayName("쿠폰 사용 실패 테스트 - 존재하지 않는 쿠폰 ID")
        void failure1() {
            // given
            long couponId = RandomGenerator.nextPositiveLong(Long.MAX_VALUE);
            List<Long> invalidCouponIds = List.of(couponId); // DB에 존재하지 않는 ID
            CouponCommand.Use command = CouponCommand.Use.of(invalidCouponIds);

            // when & then
            assertThatThrownBy(() -> sut.use(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessError.USER_COUPON_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("쿠폰 사용 실패 테스트 - 유효 기간 외 사용")
        void failure2() {
            // given
            User user = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(User.class)
                    .set("id", null)
                    .build()
                    .sample();
            entityManager.persist(user);

            LocalDateTime endedDate = LocalDateTime.now().minusDays(3);

            Coupon coupon = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Coupon.class)
                    .set("id", null)
                    .set("quantity", RandomGenerator.nextLong(1, 10))
                    .set("endedDate", endedDate)
                    .build()
                    .sample();
            entityManager.persist(coupon);

            UserCoupon userCoupon = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(UserCoupon.class)
                    .set("id", null)
                    .set("couponId", coupon.getId())
                    .set("userId", user.getId())
                    .set("used", false)
                    .build()
                    .sample();
            entityManager.persist(userCoupon);

            List<Long> expiredCouponIds = List.of(coupon.getId()); // 유효기간이 지났거나 아직 시작 전인 쿠폰 ID
            CouponCommand.Use command = CouponCommand.Use.of(expiredCouponIds);

            // when & then
            assertThatThrownBy(() -> sut.use(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessError.COUPON_EXPIRED.getMessage());
        }

        @Test
        @DisplayName("쿠폰 사용 실패 테스트 - 이미 사용한 쿠폰")
        void failure3() {
            // given
            User user = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(User.class)
                    .set("id", null)
                    .build()
                    .sample();
            entityManager.persist(user);

            Coupon coupon = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Coupon.class)
                    .set("id", null)
                    .set("quantity", RandomGenerator.nextLong(1, 10))
                    .set("endedDate", LocalDateTime.now().plusDays(3))
                    .build()
                    .sample();
            entityManager.persist(coupon);

            UserCoupon userCoupon = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(UserCoupon.class)
                    .set("id", null)
                    .set("couponId", coupon.getId())
                    .set("userId", user.getId())
                    .set("used", true)
                    .build()
                    .sample();
            entityManager.persist(userCoupon);

            List<Long> usedCouponIds = List.of(userCoupon.getId()); // 이미 사용된 쿠폰 ID
            CouponCommand.Use command = CouponCommand.Use.of(usedCouponIds);

            // when
            // then
            assertThatThrownBy(() -> sut.use(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessError.COUPON_ALREADY_USED.getMessage());
        }
    }

    @Nested
    @DisplayName("cancel 테스트")
    class CancelTest {

        @Test
        @DisplayName("쿠폰 사용 취소 성공 테스트")
        void success1() {
            // given
            User user = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(User.class)
                    .set("id", null)
                    .build()
                    .sample();
            entityManager.persist(user);

            Coupon coupon = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Coupon.class)
                    .set("id", null)
                    .set("quantity", RandomGenerator.nextLong(1, 10))
                    .build()
                    .sample();
            entityManager.persist(coupon);

            UserCoupon userCoupon = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(UserCoupon.class)
                    .set("id", null)
                    .set("couponId", coupon.getId())
                    .set("userId", user.getId())
                    .set("used", true)
                    .build()
                    .sample();
            entityManager.persist(userCoupon);

            List<Long> usedCouponIds = List.of(userCoupon.getId()); // 이미 사용된 쿠폰
            CouponCommand.Cancel command = CouponCommand.Cancel.of(usedCouponIds);
            CouponCommand.FindUserCoupons findUserCoupons = CouponCommand.FindUserCoupons.of(usedCouponIds);

            // when
            sut.cancel(command);

            // then
            List<CouponInfo.OwnedCoupon> userCouponsById = sut.findUserCouponsById(findUserCoupons);
            CouponInfo.OwnedCoupon ownedCoupon1 = userCouponsById.get(0);

            assertThat(userCouponsById).hasSize(1);
            assertThat(ownedCoupon1.getUserCouponId()).isEqualTo(userCoupon.getId());
            assertThat(ownedCoupon1.isUsed()).isFalse();
        }

        @Test
        @DisplayName("쿠폰 사용 취소 실패 테스트 - 존재하지 않는 쿠폰 ID")
        void failure1() {
            // given
            long couponId = RandomGenerator.nextPositiveLong(Long.MAX_VALUE);
            List<Long> invalidCouponIds = List.of(couponId); // 존재하지 않는 ID
            CouponCommand.Cancel command = CouponCommand.Cancel.of(invalidCouponIds);

            // when & then
            assertThatThrownBy(() -> sut.cancel(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessError.USER_COUPON_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("쿠폰 사용 취소 실패 테스트 - 사용하지 않은 쿠폰을 취소하려는 경우")
        void failure2() {
            // given
            User user = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(User.class)
                    .set("id", null)
                    .build()
                    .sample();
            entityManager.persist(user);

            Coupon coupon = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Coupon.class)
                    .set("id", null)
                    .set("quantity", RandomGenerator.nextLong(1, 10))
                    .build()
                    .sample();
            entityManager.persist(coupon);

            UserCoupon userCoupon = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(UserCoupon.class)
                    .set("id", null)
                    .set("couponId", coupon.getId())
                    .set("userId", user.getId())
                    .set("used", false)
                    .build()
                    .sample();
            entityManager.persist(userCoupon);

            List<Long> unusedCouponIds = List.of(userCoupon.getId()); // 사용되지 않은 쿠폰
            CouponCommand.Cancel command = CouponCommand.Cancel.of(unusedCouponIds);

            // when
            // then
            assertThatThrownBy(() -> sut.cancel(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessError.COUPON_NOT_USED.getMessage());
        }
    }
}