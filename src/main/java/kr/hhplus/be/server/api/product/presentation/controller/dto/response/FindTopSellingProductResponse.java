package kr.hhplus.be.server.api.product.presentation.controller.dto.response;

public record FindTopSellingProductResponse(
        Long productId,
        String name
) {
}
