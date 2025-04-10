package kr.hhplus.be.server.domain.order;

import kr.hhplus.be.server.infrastructure.order.OrderQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository {

    Optional<Order> findOrderById(long orderId);

    Order saveOrder(Order order);

    List<OrderItem> saveOrderItem(List<OrderItem> orderItems);

    List<OrderInfo.OrderHistory> findOrderDetailsByUserId(long userId);

    List<OrderQuery.OrderProjection> findOrdersByUserId(long userId);

    List<OrderQuery.OrderItemProjection> findOrderItemsByOrderId(long orderId);

    OrderQuery.OrderProjection findOrderByOrderId(long orderId);
}
