package kr.hhplus.be.server.domain.product;

import kr.hhplus.be.server.infrastructure.product.ProductQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository {

    Optional<ProductQuery.DetailProjection> findProductDetailById(long productId);

    List<ProductQuery.OptionProjection> findProductOptionsByProductId(long productId);

    List<Stock> findStocksByOptionId(List<Long> optionIds);

    List<Stock> saveStocks(List<Stock> stocks);

    Optional<Product> findProductById(long productId);

    List<ProductQuery.TopSellingProjection> findTopSellingProducts();

    List<ProductQuery.PriceOptionProjection> findProductOptionsById(List<Long> optionIds);
}
