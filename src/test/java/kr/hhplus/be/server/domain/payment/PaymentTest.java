package kr.hhplus.be.server.domain.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("Payment 테스트")
class PaymentTest {

    @Test
    @DisplayName("toEntity - 정적 팩토리 메서드가 필드를 올바르게 설정한다")
    void success_toEntity() {
        // given
        long orderId = 100L;
        long amount = 5000L;

        // when
        Payment payment = Payment.toEntity(orderId, amount);

        // then
        assertThat(payment).isNotNull();
        assertThat(payment.getOrderId()).isEqualTo(orderId);
        assertThat(payment.getAmount()).isEqualTo(amount);
        assertThat(payment.getPaymentDate()).isNotNull();
        assertThat(payment.getPaymentDate()).isBeforeOrEqualTo(LocalDateTime.now());
    }
}
