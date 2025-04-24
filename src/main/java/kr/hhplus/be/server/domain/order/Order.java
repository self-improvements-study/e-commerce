package kr.hhplus.be.server.domain.order;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.exception.BusinessError;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.common.entity.AuditableEntity;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "orders")
public class Order extends AuditableEntity {

    /**
     * 주문 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id", nullable = false, updatable = false)
    private Long id;

    /**
     * 사용자 ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 주문 일시
     */
    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    /**
     * 주문 상태
     */
    @Column(name = "status", nullable = false)
    private Status status;

    /**
     * 총 금액
     */
    @Column(name = "total_price", nullable = false)
    private Long totalPrice;

    public void success() {
        // 현재 상태가 '결제 대기'가 아니면 성공 처리할 수 없으므로 예외 발생
        if (this.status != Order.Status.PAYMENT_WAITING) {
            throw new BusinessException(BusinessError.INVALID_ORDER_STATUS);
        }
        this.status = Status.SUCCESS;
    }

    public void cancel() {
        // 현재 상태가 '결제 대기'가 아니면 취소할 수 없으므로 예외 발생
        if (this.status != Order.Status.PAYMENT_WAITING) {
            throw new BusinessException(BusinessError.INVALID_ORDER_STATUS);
        }
        this.status = Status.CANCELLED;
    }

    public enum Status {
        PAYMENT_WAITING {
            @Override
            public boolean isDetermined() {
                return false;
            }
        },

        CANCELLED {
            @Override
            public boolean isDetermined() {
                return true;
            }
        },

        SUCCESS {
            @Override
            public boolean isDetermined() {
                return true;
            }
        };

        public abstract boolean isDetermined();
    }

}
