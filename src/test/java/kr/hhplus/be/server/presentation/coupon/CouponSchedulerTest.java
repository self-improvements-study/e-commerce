package kr.hhplus.be.server.presentation.coupon;

import jakarta.persistence.EntityManager;
import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.CouponRedisRepository;
import kr.hhplus.be.server.domain.coupon.CouponRepository;
import kr.hhplus.be.server.domain.coupon.UserCoupon;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
@DisplayName("쿠폰 서비스 스케줄러 테스트")
class CouponSchedulerTest {

    @Autowired
    private CouponScheduler couponScheduler;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponRedisRepository couponRedisRepository;

    @Test
    @DisplayName("쿠폰 만료 처리 스케줄러 테스트")
    void testCouponExpiryScheduler() {
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
                .set("quantity", 10L)
                .set("endedDate", LocalDateTime.now().minusDays(1))  // 만료된 쿠폰
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

        // 스케줄러 트리거가 만료된 쿠폰을 처리하는 로직이 있을 경우 이를 호출
        couponScheduler.expireCoupons();

        // when
        List<UserCoupon> expiredCoupons = couponRepository.findUserCouponsByExpiredDate(LocalDateTime.now());

        // then
        assertThat(expiredCoupons).hasSize(0); // 만료된 쿠폰이 처리되어 없어야 함
    }

    @Nested
    @DisplayName("issuedCoupon 테스트")
    class IssuedCouponTest {

        @Test
        @DisplayName("선착순 쿠폰 발급 완료 테스트")
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
                    .set("quantity", 1L)
                    .set("endedDate", LocalDateTime.now().plusDays(1))  // 만료된 쿠폰
                    .set("status", Coupon.Status.AVAILABLE)
                    .build()
                    .sample();
            entityManager.persist(coupon);

            couponRedisRepository.addCouponRequestToQueue(user.getId(), coupon.getId());

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
    }
}