package kr.hhplus.be.server.api.order.presentation.controller.dto.request;

import jakarta.annotation.Nullable;

import java.util.List;

public record CreateOrderRequest(
        List<CreateOrderItemRequest> orderItems
) {

    public record CreateOrderItemRequest(
            Long optionId,
            Long quantity,
            @Nullable
            Long userCouponId
    ) {
    }

}
