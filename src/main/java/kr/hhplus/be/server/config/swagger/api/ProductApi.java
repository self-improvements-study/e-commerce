package kr.hhplus.be.server.config.swagger.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.api.product.presentation.controller.dto.response.FindTopSellingProductResponse;
import kr.hhplus.be.server.api.product.presentation.controller.dto.response.FindProductResponse;
import kr.hhplus.be.server.common.response.CommonResponse;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Tag(name = "Product", description = "상품 관련 API")
public interface ProductApi {

    @Operation(summary = "상품 조회", description = "특정 상품의 상세 정보를 조회합니다.")
    CommonResponse<FindProductResponse> findProduct(
            @Parameter(description = "상품 ID", example = "1")@PathVariable Long productId);

    @Operation(summary = "판매 우수 상품 조회", description = "가장 많이 판매된 상품 목록을 조회합니다.")
    CommonResponse<List<FindTopSellingProductResponse>> findTopSellingProducts();
}
