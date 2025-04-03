package kr.hhplus.be.server.api.order.presentation.controller.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record FindOrderDetailResponse(
        Long orderId,
        String status,
        Long totalAmount,
        List<FindOrderItemDetailResponse> orderItems,
        FindPaymentResponse payment
) {

    public record FindOrderItemDetailResponse(
            Long optionId,
            String productName,
            String size,
            String color,
            Long quantity,
            Long price
    ) {
    }

    public record FindPaymentResponse (
        Long paymentId,
        Long orderId,
        Long amount,
        LocalDateTime paymentDate
    ) {

    }

}
