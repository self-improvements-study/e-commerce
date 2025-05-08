package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.domain.product.ProductInfo;
import kr.hhplus.be.server.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Component
public class ProductFacade {

    private final ProductService productService;

    @Transactional(readOnly = true)
    public ProductResult.Detail getProduct(long productId) {
        ProductInfo.Detail product = productService.getProductById(productId);
        return ProductResult.Detail.from(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResult.TopSelling> findTopSellingProducts() {

        LocalDate daysAgo = LocalDate.now().minusDays(3);
        long limit = 5;

        ProductInfo.ProductSalesData productSalesData = productService.getTopSellingProducts(daysAgo, limit);

        List<ProductInfo.TopSelling> topSellingProducts = productSalesData.getList();

        return topSellingProducts.stream()
                .map(ProductResult.TopSelling::from)
                .toList();
    }
}
