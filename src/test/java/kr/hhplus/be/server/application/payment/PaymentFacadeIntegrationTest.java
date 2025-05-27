package kr.hhplus.be.server.application.payment;

import jakarta.persistence.EntityManager;
import kr.hhplus.be.server.common.exception.BusinessError;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderEvent;
import kr.hhplus.be.server.domain.order.OrderEventPublisher;
import kr.hhplus.be.server.domain.order.OrderRepository;
import kr.hhplus.be.server.domain.payment.*;
import kr.hhplus.be.server.domain.point.Point;
import kr.hhplus.be.server.domain.point.PointRepository;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.test.util.RandomGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
@DisplayName("결제서비스 통합 테스트")
class PaymentFacadeIntegrationTest {

    @Autowired
    private PaymentFacade paymentFacade;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointRepository pointRepository;

    @MockitoBean
    private PaymentEventPublisher paymentEventPublisher;

    @Nested
    @DisplayName("결제 성공 이벤트 발행 테스트")
    class PaymentTest {

        @Test
        @DisplayName("성공 - 결제 성공 시 이벤트 발행 여부 확인")
        void success1() {

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

            // when
            paymentFacade.payment(savedOrder.getUserId(), savedOrder.getId());

            // then
            verify(paymentEventPublisher, times(1)).publish(any(PaymentEvent.CreatePayment.class));
        }

        @Test
        @DisplayName("실패 - 잘못된 주문 ID로 이벤트 발행하지 않음")
        void failure1() {
            // given
            User user = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(User.class)
                    .set("id", null)
                    .build()
                    .sample();

            User savedUser = userRepository.save(user);

            long balance = 0;
            Point point = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Point.class)
                    .set("id", null)
                    .set("userId", user.getId())
                    .set("balance", balance)
                    .build()
                    .sample();

            pointRepository.save(point);

            Order order = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Order.class)
                    .set("id", null)
                    .set("userId", savedUser.getId())
                    .set("status", Order.Status.PAYMENT_WAITING)
                    .set("totalPrice", -1L)
                    .build()
                    .sample();

            Order savedOrder = orderRepository.saveOrder(order);

            // when & then
            assertThatThrownBy(() -> paymentFacade.payment(savedOrder.getUserId(), savedOrder.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessError.PAYMENT_AMOUNT_DECREASE_TOO_SMALL.getMessage());

            verify(paymentEventPublisher, times(1)).publish(any(PaymentEvent.CreatePayment.class));
        }
    }
}