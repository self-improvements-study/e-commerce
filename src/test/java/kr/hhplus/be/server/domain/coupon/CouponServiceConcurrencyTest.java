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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
@DisplayName("쿠폰서비스 동시성 테스트")
class CouponServiceConcurrencyTest {

    @Autowired
    CouponService sut;

    @Autowired
    private EntityManager entityManager;

    @Nested
    @DisplayName("issueCoupon 테스트")
    class IssueCouponTest {

        @Test
        @DisplayName("쿠폰 발급 동시성 테스트")
        void test() throws InterruptedException {
            // given
            long couponQuantity = 5;

            Coupon coupon = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Coupon.class)
                    .set("id", null)
                    .set("quantity", couponQuantity)
                    .build()
                    .sample();
            entityManager.persist(coupon);

            int threadCount = 10;
            CouponCommand.IssuedCoupon command = new CouponCommand.IssuedCoupon(1L, 1L);
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);

            List<Throwable> thrownExceptions = Collections.synchronizedList(new ArrayList<>());
            AtomicInteger successCount = new AtomicInteger();

            // when
            for (int i = 0; i < threadCount; i++) {
                executorService.execute(() -> {
                    try {
                        sut.issueCoupon(command);
                        successCount.incrementAndGet();
                    } catch (Throwable t) {
                        thrownExceptions.add(t);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executorService.shutdown();

            // then
            assertThat(thrownExceptions).allSatisfy(e -> {
                assertThat(e).isInstanceOf(BusinessException.class);
                assertThat(((BusinessException) e).getBusinessError()).isEqualTo(BusinessError.COUPON_ISSUE_LIMIT_EXCEEDED);
            });
        }

        @Test
        @DisplayName("쿠폰 발급 동시성 테스트 - 여러 유저가 동시에 시도")
        void test_multiple_users() throws InterruptedException {
            // given
            long couponQuantity = 5;

            Coupon coupon = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Coupon.class)
                    .set("id", null)
                    .set("quantity", couponQuantity)
                    .build()
                    .sample();
            entityManager.persist(coupon);

            int threadCount = 10; // 10명의 서로 다른 유저가 시도
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);

            List<Throwable> thrownExceptions = Collections.synchronizedList(new ArrayList<>());
            AtomicInteger successCount = new AtomicInteger();

            // when
            for (int i = 0; i < threadCount; i++) {
                final long userId = i + 1; // 서로 다른 userId 사용
                CouponCommand.IssuedCoupon command = new CouponCommand.IssuedCoupon(userId, coupon.getId());
                executorService.execute(() -> {
                    try {
                        sut.issueCoupon(command);
                        successCount.incrementAndGet();
                    } catch (Throwable t) {
                        thrownExceptions.add(t);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executorService.shutdown();

            // then
            assertThat(thrownExceptions).allSatisfy(e -> {
                assertThat(e).isInstanceOf(BusinessException.class);
                assertThat(((BusinessException) e).getBusinessError()).isEqualTo(BusinessError.COUPON_ISSUE_LIMIT_EXCEEDED);
            });
        }

    }
}