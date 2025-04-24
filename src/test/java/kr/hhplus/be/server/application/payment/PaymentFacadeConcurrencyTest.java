package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.application.order.OrderCriteria;
import kr.hhplus.be.server.application.order.OrderFacade;
import kr.hhplus.be.server.domain.order.*;
import kr.hhplus.be.server.domain.payment.PaymentInfo;
import kr.hhplus.be.server.domain.payment.PaymentService;
import kr.hhplus.be.server.domain.point.Point;
import kr.hhplus.be.server.domain.point.PointRepository;
import kr.hhplus.be.server.domain.product.*;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.test.util.RandomGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@DisplayName("결제 파사드 동시성 테스트")
class PaymentFacadeConcurrencyTest {

    @Autowired
    private PaymentFacade paymentFacade;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointRepository pointRepository;

    @Nested
    @DisplayName("payment 테스트")
    class PaymentTest {

        @Test
        @DisplayName("결제 동시성 테스트")
        void test() throws InterruptedException {
            // given
            User user = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(User.class)
                    .set("id", null)
                    .build()
                    .sample();

            User savedUser = userRepository.save(user);

            long balance = 500000L;
            Point point = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Point.class)
                    .set("id", null)
                    .set("userId", user.getId())
                    .set("balance", balance)
                    .build()
                    .sample();

            pointRepository.save(point);

            long totalPrice = RandomGenerator.nextLong(10000, 100000);

            Order order = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Order.class)
                    .set("id", null)
                    .set("userId", savedUser.getId())
                    .set("status", Order.Status.PAYMENT_WAITING)
                    .set("totalPrice", totalPrice)
                    .build()
                    .sample();

            Order savedOrder = orderRepository.saveOrder(order);

            int threadCount = 5;

            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);

            // when
            for (int i = 0; i < threadCount; i++) {
                executorService.execute(() -> {
                    try {
                        paymentFacade.payment(savedOrder.getUserId(), savedOrder.getId());
                        successCount.incrementAndGet();
                    } catch (Throwable t) {
                        failureCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executorService.shutdown();

            // then
            assertThat(successCount.get()).isEqualTo(1);
            assertThat(failureCount.get()).isEqualTo(4);

            PaymentInfo.PaymentSummary paymentHistory = paymentService.paymentHistory(savedOrder.getId());

            assertThat(paymentHistory).isNotNull();
            assertThat(paymentHistory.getAmount()).isEqualTo(totalPrice);
        }
    }
}