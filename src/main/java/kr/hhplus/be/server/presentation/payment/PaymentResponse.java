package kr.hhplus.be.server.presentation.payment;

import kr.hhplus.be.server.application.payment.PaymentResult;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public record PaymentResponse() {

    @Getter
    @Builder
    public static class Create {
        private final long orderId;
        private final long paymentId;
        private final long amount;
        private final LocalDateTime paymentDate;

        public static Create from(PaymentResult.PaymentSummary summary) {
            return Create.builder()
                    .orderId(summary.getOrderId())
                    .paymentId(summary.getPaymentId())
                    .amount(summary.getAmount())
                    .paymentDate(summary.getPaymentDate())
                    .build();
        }
    }
}