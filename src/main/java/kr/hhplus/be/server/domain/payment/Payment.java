package kr.hhplus.be.server.domain.payment;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.common.entity.AuditableEntity;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "payment")
public class Payment extends AuditableEntity {

    /**
     * 결제 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id", nullable = false, updatable = false)
    private Long id;

    /**
     * 주문 ID
     */
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    /**
     * 결제 금액
     */
    @Column(name = "amount", nullable = false)
    private Long amount;

    /**
     * 결제일시
     */
    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    public static Payment toEntity(long orderId, long amount) {
        Payment payment = new Payment();
        payment.orderId = orderId;
        payment.amount = amount;
        payment.paymentDate = LocalDateTime.now();

        return payment;
    }
}