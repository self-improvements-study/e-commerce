package kr.hhplus.be.server.domain.order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Optional<Order> findOrderById(long orderId);

    Order saveOrder(Order order);

    List<OrderItem> saveOrderItem(List<OrderItem> orderItems);

    List<Order> findOrdersByUserId(long userId);

    List<OrderQuery.OrderItemProjection> findOrderItemByOrderId(long orderId);

    List<Order> findExpiredOrders(LocalDateTime standardDateTime);

}
