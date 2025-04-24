package kr.hhplus.be.server.infrastructure.order.repository;

import kr.hhplus.be.server.domain.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(long userId);

    @Query("""
                SELECT o
                  FROM Order o
                 WHERE o.orderDate < :standardDateTime
                   AND o.status = 0
            """)
    List<Order> findExpiredOrders(@Param("standardDateTime") LocalDateTime standardDateTime);

}
