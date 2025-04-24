package kr.hhplus.be.server.infrastructure.payment;

import kr.hhplus.be.server.domain.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(long orderId);
}
