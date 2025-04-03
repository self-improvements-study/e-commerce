package kr.hhplus.be.server.api.order.presentation.controller.dto.response;

import java.util.List;

public record FindOrderResponse(
        Long orderId,
        String status,
        Long totalAmount,
        List<FindOrderItemResponse> orderItems
) {

    public record FindOrderItemResponse(
            Long optionId,
            String productName,
            String size,
            String color,
            Long quantity,
            Long price
    ) {
    }

}
