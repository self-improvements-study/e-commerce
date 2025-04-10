package kr.hhplus.be.server.domain.payment;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findByOrderId(long orderId);
}
