package kr.hhplus.be.server.api.product.presentation.controller.dto.response;

import java.util.List;

public record FindProductResponse(
        Long productId,
        String name,
        Long price,
        List<FindProductOptionResponse> options
) {
    public record FindProductOptionResponse(
            Long optionId,
            String size,
            String color,
            int stock
    ) {
    }
}