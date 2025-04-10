package kr.hhplus.be.server.presentation.payment;

import lombok.Builder;
import lombok.Getter;

public record PaymentRequest() {

    @Getter
    @Builder
    public static class Create {
        private final long userId;
        private final long orderId;
    }
}
