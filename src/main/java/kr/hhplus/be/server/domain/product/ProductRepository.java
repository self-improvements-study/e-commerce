package kr.hhplus.be.server.domain.product;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    List<ProductQuery.Option> findProductOptionsByProductId(long productId);

    List<Stock> findStocksByOptionId(List<Long> optionIds);

    List<Stock> findByProductOptionIdInWithLock(List<Long> optionIds);

    List<Stock> saveStocks(List<Stock> stocks);

    Optional<Product> findProductById(long productId);

    List<ProductQuery.TopSelling> findTopSellingProducts(LocalDate daysAgo, long limit);

    List<ProductQuery.PriceOption> findProductOptionsById(List<Long> optionIds);

    Product saveProduct(Product product);

    List<ProductOption> saveProductOption(List<ProductOption> product);
}
