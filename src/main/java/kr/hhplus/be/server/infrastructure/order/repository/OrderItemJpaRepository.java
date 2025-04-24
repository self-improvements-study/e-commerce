package kr.hhplus.be.server.infrastructure.order.repository;

import kr.hhplus.be.server.domain.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemJpaRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(long orderId);
}
