package kr.hhplus.be.server.domain.point;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.common.entity.AuditableEntity;
import kr.hhplus.be.server.common.exception.BusinessError;
import kr.hhplus.be.server.common.exception.BusinessException;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "point")
public class Point extends AuditableEntity {

    /**
     * 포인트 아이디
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_id", nullable = false, updatable = false)
    private Long id;

    /**
     * 사용자 아이디
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 잔액
     */
    @Column(name = "balance", nullable = false)
    private Long balance;

    @Version
    private Long version;

    private static final long MAX_POINT = 1_000_000L;

    public void increase(long amount) {

        // 1. 0 이하인 경우 → 잘못된 충전 요청
        if (amount <= 0) {
            throw new BusinessException(BusinessError.POINT_AMOUNT_INCREASE_TOO_SMALL);
        }

        // 2. 충전 금액이 최대 포인트 한도보다 큰 경우 → 제한 초과
        if (amount > MAX_POINT) {
            throw new BusinessException(BusinessError.POINT_AMOUNT_INCREASE_EXCEEDS_LIMIT);
        }

        // 3. 현재 잔액 + 충전 금액이 최대 한도를 초과하는 경우 → 충전 불가
        if (amount + this.balance > MAX_POINT) {
            throw new BusinessException(BusinessError.POINT_AMOUNT_INCREASE_EXCEEDS_LIMIT);
        }

        this.balance += amount;
    }

    public void decrease(long amount) {

        // 1. 0 이하인 경우 → 잘못된 차감 요청
        if (amount <= 0) {
            throw new BusinessException(BusinessError.POINT_AMOUNT_DECREASE_TOO_SMALL);
        }

        // 2. 차감 금액이 최대 포인트 한도를 초과한 경우 → 제한 위반
        if (amount > MAX_POINT) {
            throw new BusinessException(BusinessError.POINT_AMOUNT_DECREASE_EXCEEDS_LIMIT);
        }

        // 3. 현재 잔액보다 차감하려는 금액이 큰 경우 → 잔액 초과
        if (balance - amount < 0) {
            throw new BusinessException(BusinessError.POINT_BALANCE_EXCEEDED);
        }

        this.balance -= amount;
    }
}
