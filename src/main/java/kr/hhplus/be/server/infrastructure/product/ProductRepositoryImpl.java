package kr.hhplus.be.server.infrastructure.product;

import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.product.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static kr.hhplus.be.server.domain.order.QOrder.order;
import static kr.hhplus.be.server.domain.order.QOrderItem.orderItem;
import static kr.hhplus.be.server.domain.product.QProduct.product;
import static kr.hhplus.be.server.domain.product.QProductOption.productOption;
import static kr.hhplus.be.server.domain.product.QStock.stock;

@RequiredArgsConstructor
@Repository
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;
    private final ProductOptionJpaRepository productOptionJpaRepository;
    private final StockJpaRepository stockJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public List<ProductQuery.Option> findProductOptionsByProductId(long productId) {
        return queryFactory
                .select(new QProductQuery_Option(
                        productOption.id.as("optionId"),
                        productOption.size,
                        productOption.color,
                        stock.quantity.as("stockQuantity")
                ))
                .from(productOption)
                .join(stock).on(productOption.id.eq(stock.productOptionId))
                .where(productOption.productId.eq(productId))
                .fetch();
    }

    @Override
    public List<Stock> findStocksByOptionId(List<Long> optionIds) {
        return stockJpaRepository.findByProductOptionIdIn(optionIds);
    }

    @Override
    public List<Stock> findByProductOptionIdInWithLock(List<Long> optionIds) {
        return stockJpaRepository.findByProductOptionIdInWithLock(optionIds);
    }

    @Override
    public List<Stock> saveStocks(List<Stock> stocks) {
        return stockJpaRepository.saveAll(stocks);
    }

    @Override
    public Optional<Product> findProductById(long productId) {
        return productJpaRepository.findById(productId);
    }

    @Override
    public List<ProductQuery.TopSelling> findTopSellingProducts(LocalDateTime daysAgo, long limit) {
        NumberExpression<Long> salesCount = orderItem.quantity.sum().castToNum(Long.class);

        return queryFactory
                .select(new QProductQuery_TopSelling(
                        product.id,
                        product.name,
                        salesCount
                ))
                .from(product)
                .join(productOption).on(product.id.eq(productOption.productId))
                .join(orderItem).on(productOption.id.eq(orderItem.optionId))
                .join(order).on(order.id.eq(orderItem.orderId))
                .where(
                        order.status.eq(Order.Status.SUCCESS),
                        order.orderDate.goe(daysAgo)
                )
                .groupBy(product.id)
                .orderBy(salesCount.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<ProductQuery.PriceOption> findProductOptionsById(List<Long> optionIds) {
        return queryFactory
                .select(new QProductQuery_PriceOption(
                        productOption.id.as("optionId"),
                        product.price.as("price"),
                        stock.quantity.as("stockQuantity")
                ))
                .from(productOption)
                .join(stock).on(productOption.id.eq(stock.productOptionId))
                .join(product).on(productOption.productId.eq(product.id))
                .where(productOption.id.in(optionIds))
                .fetch();
    }

    @Override
    public Product saveProduct(Product product) {
        return productJpaRepository.save(product);
    }

    @Override
    public List<ProductOption> saveProductOption(List<ProductOption> product) {
        return productOptionJpaRepository.saveAll(product);
    }
}
