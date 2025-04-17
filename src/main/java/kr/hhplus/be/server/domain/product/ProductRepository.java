package kr.hhplus.be.server.domain.product;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {

//    Optional<ProductQuery.Detail> findProductDetailById(long productId);

    List<ProductQuery.Option> findProductOptionsByProductId(long productId);

    List<Stock> findStocksByOptionId(List<Long> optionIds);

    List<Stock> saveStocks(List<Stock> stocks);

    Optional<Product> findProductById(long productId);

    List<ProductQuery.TopSelling> findTopSellingProducts(LocalDateTime daysAgo, long limit);

    List<ProductQuery.PriceOption> findProductOptionsById(List<Long> optionIds);
}