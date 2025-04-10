package kr.hhplus.be.server.presentation.product;

import kr.hhplus.be.server.application.product.ProductFacade;
import kr.hhplus.be.server.application.product.ProductResult;
import kr.hhplus.be.server.config.swagger.api.ProductApi;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/products")
public class ProductController implements ProductApi {

    private final ProductFacade productFacade;

    // 상품 조회
    @GetMapping("/{productId}")
    public ProductResponse.Detail findProduct(@PathVariable Long productId) {
        ProductResult.Detail product = productFacade.getProduct(productId);
        return ProductResponse.Detail.from(product);
    }

    // 판매 우수 상품 조회
    @GetMapping("/top-sellers")
    public List<ProductResponse.TopSelling> findTopSellingProducts() {
        List<ProductResult.TopSelling> result = productFacade.findTopSellingProducts();
        return ProductResponse.TopSelling.from(result);

    }
}
