package kr.hhplus.be.server.api.payment.presentation.controller.dto.response;

import java.time.LocalDateTime;

public record CreatePaymentResponse(
        Long orderId,
        Long paymentId,
        Long amount,
        LocalDateTime paymentDate
) {
}
