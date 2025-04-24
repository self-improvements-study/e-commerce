package kr.hhplus.be.server.domain.order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Optional<Order> findOrderById(long orderId);

    Order saveOrder(Order order);

    List<OrderItem> saveOrderItem(List<OrderItem> orderItems);

    List<Order> findOrdersByUserId(long userId);

    List<OrderQuery.OrderItemProjection> findOrderItemByOrderId(long orderId);
}
