package kr.hhplus.be.server.api.product.presentation.controller;

import kr.hhplus.be.server.api.product.presentation.controller.dto.response.FindTopSellingProductResponse;
import kr.hhplus.be.server.api.product.presentation.controller.dto.response.FindProductResponse;
import kr.hhplus.be.server.common.response.CommonResponse;
import kr.hhplus.be.server.config.swagger.api.ProductApi;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController implements ProductApi {

    // 상품 조회
    @GetMapping("/{productId}")
    public CommonResponse<FindProductResponse> findProduct(@PathVariable Long productId) {
        FindProductResponse data = new FindProductResponse(
                1L,
                "운동화",
                99000L,
                List.of(
                        new FindProductResponse.FindProductOptionResponse(
                                101L,
                                "260mm",
                                "화이트",
                                13
                        ),
                        new FindProductResponse.FindProductOptionResponse(
                                102L,
                                "270mm",
                                "블랙",
                                23
                        )
                )
        );

        return CommonResponse.success(data);
    }

    // 판매 우수 상품 조회
    @GetMapping("/top-sellers")
    public CommonResponse<List<FindTopSellingProductResponse>> findTopSellingProducts() {
        List<FindTopSellingProductResponse> data = List.of(
                new FindTopSellingProductResponse(1L, "스마트폰"),
                new FindTopSellingProductResponse(2L, "태블릿")
        );

        return CommonResponse.success(data);
    }
}
