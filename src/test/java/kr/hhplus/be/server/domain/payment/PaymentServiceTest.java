package kr.hhplus.be.server.domain.payment;

import kr.hhplus.be.server.common.exception.BusinessError;
import kr.hhplus.be.server.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService 테스트")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Nested
    @DisplayName("결제")
    class PaymentTest {

        @Test
        @DisplayName("결제 정보 저장 성공")
        void success1() {
            // given
            PaymentCommand.Payment command = PaymentCommand.Payment.of(1L, 1000L);
            Payment payment = Payment.builder()
                    .id(1L)
                    .orderId(command.getOrderId())
                    .amount(command.getAmount())
                    .paymentDate(LocalDateTime.now())
                    .build();

            when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

            // when
            PaymentInfo.PaymentSummary result = paymentService.payment(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getPaymentId()).isEqualTo(1L);
            assertThat(result.getOrderId()).isEqualTo(command.getOrderId());
            assertThat(result.getAmount()).isEqualTo(command.getAmount());
            assertThat(result.getPaymentDate()).isNotNull();
        }
    }

    @Nested
    @DisplayName("결제 내역 조회")
    class PaymentHistoryTest {

        @Test
        @DisplayName("결제 내역 조회 성공")
        void success1() {
            // given
            long orderId = 1L;
            Payment payment = Payment.builder()
                    .id(1L)
                    .orderId(orderId)
                    .amount(2000L)
                    .paymentDate(LocalDateTime.now())
                    .build();

            when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(payment));

            // when
            PaymentInfo.PaymentSummary result = paymentService.paymentHistory(orderId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getOrderId()).isEqualTo(orderId);
            assertThat(result.getAmount()).isEqualTo(2000L);
        }

        @Test
        @DisplayName("결제 내역이 없으면 예외 발생")
        void failure1() {
            // given
            long orderId = 2L;
            when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> paymentService.paymentHistory(orderId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(BusinessError.PAYMENT_NOT_FOUND.getMessage());
        }
    }
}
