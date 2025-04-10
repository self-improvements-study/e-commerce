package kr.hhplus.be.server.domain.coupon;

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
@Table(name = "coupon")
public class Coupon extends AuditableEntity {

    /**
     * 쿠폰 아이디
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_id", nullable = false, updatable = false)
    private Long id;

    /**
     * 쿠폰 이름
     */
    @Column(name = "coupon_name", nullable = false)
    private String couponName;

    /**
     * 할인 금액
     */
    @Column(name = "discount", nullable = false)
    private Long discount;

    /**
     * 쿠폰 개수
     */
    @Column(name = "quantity", nullable = false)
    private Long quantity;

    /**
     * 쿠폰 사용 시작 시간
     */
    @Column(name = "started_date", nullable = false)
    private LocalDateTime startedDate;

    /**
     * 쿠폰 만료 시간
     */
    @Column(name = "ended_date", nullable = false)
    private LocalDateTime endedDate;

    public void decrease(long quantity) {

        if (quantity <= 0) {
            throw new BusinessException(BusinessError.INVALID_COUPON_QUANTITY);
        }
        if (this.quantity < quantity) {
            throw new BusinessException(BusinessError.COUPON_ISSUE_LIMIT_EXCEEDED);
        }

        this.quantity -= quantity;
    }
}
