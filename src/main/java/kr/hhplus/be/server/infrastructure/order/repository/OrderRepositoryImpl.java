package kr.hhplus.be.server.infrastructure.order.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.hhplus.be.server.domain.order.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static kr.hhplus.be.server.domain.order.QOrder.order;
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
                        product.id,
                        product.name,
                        productOption.size,
                        productOption.color,
                        orderItem.quantity,
                        orderItem.userCouponId,
                        orderItem.originalPrice,
                        order.orderDate
                ))
                .from(orderItem)
                .join(productOption).on(orderItem.optionId.eq(productOption.id))
                .join(product).on(productOption.productId.eq(product.id))
                .join(order).on(orderItem.orderId.eq(order.id))
                .where(orderItem.orderId.eq(orderId))
                .fetch();
    }

    @Override
    public List<Order> findExpiredOrders(LocalDateTime standardDateTime) {
        return orderJpaRepository.findExpiredOrders(standardDateTime);
    }

}
