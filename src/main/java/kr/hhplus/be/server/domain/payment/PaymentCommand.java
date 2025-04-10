package kr.hhplus.be.server.domain.payment;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentCommand {

    @Getter
    @Builder
    public static class Payment {
        private long orderId;
        private long amount;

        public static Payment of(long orderId, long amount) {
            return Payment.builder()
                    .orderId(orderId)
                    .amount(amount)
                    .build();
        }
    }


}
