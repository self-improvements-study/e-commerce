package kr.hhplus.be.server.domain.payment;

import jakarta.persistence.EntityManager;
import kr.hhplus.be.server.common.exception.BusinessError;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.coupon.CouponService;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.test.util.RandomGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
@DisplayName("결제서비스 통합 테스트")
class PaymentServiceIntegrationTest {

    @Autowired
    PaymentService sut;

    @Autowired
    private EntityManager entityManager;

    @Nested
    @DisplayName("payment 테스트")
    class PaymentTest {

        @Test
        @DisplayName("성공 - 정상적인 결제 요청")
        void success1() {
            // given
            long totalPrice = RandomGenerator.nextLong(10000, 100000);

            Order order = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Order.class)
                    .set("id", null)
                    .set("userId", 1L)
                    .set("status", Order.Status.PAYMENT_WAITING)
                    .set("totalPrice", totalPrice)
                    .build()
                    .sample();
            entityManager.persist(order);

            PaymentCommand.Payment command = new PaymentCommand.Payment(
                    1L,
                    order.getId(),
                    order.getTotalPrice(),
                    List.of(new PaymentCommand.Payment.OptionStock(1L, 1)),
                    List.of(new PaymentCommand.Payment.ProductSignal(1L, LocalDate.now(), "foo", 1)),
                    List.of(new PaymentCommand.Payment.UserCoupon(1L))
            );

            // when
            PaymentInfo.PaymentSummary result = sut.payment(command);


            // then
            PaymentInfo.PaymentSummary paymentSummary = sut.paymentHistory(result.getOrderId());

            assertThat(result).isNotNull();
            assertThat(result.getOrderId()).isEqualTo(order.getId());
            assertThat(result.getAmount()).isEqualTo(order.getTotalPrice());
            assertThat(result.getAmount()).isEqualTo(paymentSummary.getAmount());
        }

        @Test
        @DisplayName("실패 - 결제 금액이 음수인 경우 예외 발생")
        void failure1() {
            // given
            long totalPrice = RandomGenerator.nextNegativeLong(-10L);

            Order order = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Order.class)
                    .set("id", null)
                    .set("userId", 1L)
                    .set("status", Order.Status.PAYMENT_WAITING)
                    .set("totalPrice", totalPrice)
                    .build()
                    .sample();
            entityManager.persist(order);

            PaymentCommand.Payment command = new PaymentCommand.Payment(
                    1L,
                    order.getId(),
                    order.getTotalPrice(),
                    List.of(new PaymentCommand.Payment.OptionStock(1L, 1)),
                    List.of(new PaymentCommand.Payment.ProductSignal(1L, LocalDate.now(), "foo", 1)),
                    List.of(new PaymentCommand.Payment.UserCoupon(1L))
            );

            // when & then
            assertThatThrownBy(() -> sut.payment(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessError.PAYMENT_AMOUNT_DECREASE_TOO_SMALL.getMessage());
        }
    }
}