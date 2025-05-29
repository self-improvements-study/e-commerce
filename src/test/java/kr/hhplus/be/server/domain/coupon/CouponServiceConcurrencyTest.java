package kr.hhplus.be.server.domain.coupon;

import kr.hhplus.be.server.test.util.RandomGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@DisplayName("쿠폰서비스 동시성 테스트")
class CouponServiceConcurrencyTest {

    @Autowired
    CouponService sut;

    @Autowired
    CouponRepository couponRepository;

    @Nested
    @DisplayName("issueCoupon 테스트")
    class IssueCouponTest {

        @Test
        @DisplayName("쿠폰 발급 동시성 테스트")
        void test() throws InterruptedException {
            // given
            Long totalQuantity = 5L; // 쿠폰 수량 5
            Long userCount = 10L;    // 10명이 동시에 발급 시도

            Coupon coupon = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Coupon.class)
                    .set("id", null)
                    .set("quantity", totalQuantity)
                    .build()
                    .sample();

            Coupon saved = couponRepository.save(coupon);

            int threadCount = 10;

            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);

            List<Throwable> thrownExceptions = Collections.synchronizedList(new ArrayList<>());
            AtomicInteger successCount = new AtomicInteger();

            // when
            for (int i = 0; i < threadCount; i++) {
                long userId = i + 1;
                CouponCommand.IssuedCoupon issuedCoupon = new CouponCommand.IssuedCoupon(userId, saved.getId());
                List<CouponCommand.IssuedCoupon> list = List.of(issuedCoupon);
                CouponCommand.IssuedCouponBatch command = new CouponCommand.IssuedCouponBatch(saved.getId(), list);

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
            Optional<Coupon> couponById = couponRepository.findCouponById(saved.getId());

            assertThat(couponById.isPresent()).isTrue();
            assertThat(couponById.get().getQuantity()).isEqualTo(0);
            assertThat((long) successCount.get()).isEqualTo(totalQuantity); // 성공한 발급은 정확히 수량만큼
            assertThat(thrownExceptions).hasSize((int) (userCount - totalQuantity)); // 실패한 수 만큼 예외 발생
        }
    }
}