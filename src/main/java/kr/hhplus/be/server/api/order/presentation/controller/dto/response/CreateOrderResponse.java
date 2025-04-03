package kr.hhplus.be.server.api.order.presentation.controller.dto.response;

public record CreateOrderResponse(
        Long orderId,
        Long totalAmount,
        String status
) {
}
