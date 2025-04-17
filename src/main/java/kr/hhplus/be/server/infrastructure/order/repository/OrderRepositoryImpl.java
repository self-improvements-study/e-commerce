package kr.hhplus.be.server.infrastructure.order.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.hhplus.be.server.domain.order.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static kr.hhplus.be.server.domain.order.QOrderItem.orderItem;
import static kr.hhplus.be.server.domain.product.QProduct.product;
import static kr.hhplus.be.server.domain.product.QProductOption.productOption;

@RequiredArgsConstructor
@Repository
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderItemJpaRepository orderItemJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Order> findOrderById(long orderId) {
        return orderJpaRepository.findById(orderId);
    }

    @Override
    public Order saveOrder(Order order) {
        return orderJpaRepository.save(order);
    }

    @Override
    public List<OrderItem> saveOrderItem(List<OrderItem> orderItems) {
        return orderItemJpaRepository.saveAll(orderItems);
    }

    @Override
    public List<Order> findOrdersByUserId(long userId) {
        return orderJpaRepository.findByUserId(userId);
    }

    @Override
    public List<OrderQuery.OrderItemProjection> findOrderItemByOrderId(long orderId) {
        return queryFactory
                .select(new QOrderQuery_OrderItemProjection(
                        orderItem.optionId,
                        product.name,
                        productOption.size,
                        productOption.color,
                        orderItem.quantity,
                        orderItem.userCouponId,
                        orderItem.originalPrice
                ))
                .from(orderItem)
                .join(productOption).on(orderItem.optionId.eq(productOption.id))
                .join(product).on(productOption.productId.eq(product.id))
                .where(orderItem.orderId.eq(orderId))
                .fetch();
    }
}
