package kr.hhplus.be.server.domain.point;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.common.entity.AuditableEntity;
import lombok.*;

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "point_history")
public class PointHistory extends AuditableEntity {

    /**
     * 포인트 이력 아이디
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_history_id", nullable = false, updatable = false)
    private Long id;

    /**
     * 사용자 아이디
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 변경 금액
     */
    @Column(name = "amount", nullable = false)
    private Long amount;

    /**
     * 거래 유형
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private Type type;

    public static PointHistory ofCharge(long userId, long amount) {
        PointHistory history = new PointHistory();
        history.userId = userId;
        history.amount = amount;
        history.type = Type.CHARGE;

        return history;
    }

    public static PointHistory ofPayment(long userId, long amount) {
        PointHistory history = new PointHistory();
        history.userId = userId;
        history.amount = amount;
        history.type = Type.PAYMENT;

        return history;
    }

    public enum Type {
        CHARGE, PAYMENT
    }

}
