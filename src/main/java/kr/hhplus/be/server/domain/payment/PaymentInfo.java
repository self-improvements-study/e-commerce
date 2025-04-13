package kr.hhplus.be.server.domain.payment;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PaymentInfo {

    @Getter
    @Builder
    public static class PaymentSummary {
        private long paymentId;
        private long orderId;
        private long amount;
        private LocalDateTime paymentDate;

        public static PaymentSummary from(Payment entity) {
            return PaymentSummary.builder()
                    .paymentId(entity.getId())
                    .orderId(entity.getOrderId())
                    .amount(entity.getAmount())
                    .paymentDate(entity.getPaymentDate())
                    .build();
        }
    }

}
