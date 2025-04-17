package kr.hhplus.be.server.domain.coupon;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.exception.BusinessError;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.common.entity.AuditableEntity;
import lombok.*;
import org.hibernate.type.YesNoConverter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user_coupon")
public class UserCoupon extends AuditableEntity {

    /**
     * 유저 쿠폰 아이디
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_coupon_id", nullable = false, updatable = false)
    private Long id;

    /**
     * 유저 아이디
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 쿠폰 아이디
     */
    @Column(name = "coupon_id", nullable = false)
    private Long couponId;

    /**
     * 사용 여부
     */
    @Convert(converter = YesNoConverter.class)
    @Column(name = "used", nullable = false)
    private boolean used;

    /**
     * 쿠폰 사용 처리
     */
    public void use() {
        if (this.used) {
            throw new BusinessException(BusinessError.COUPON_ALREADY_USED);
        }
        this.used = true;
    }

    /**
     * 쿠폰 미사용 처리
     */
    public void cancel() {
        if (!this.used) {
            throw new BusinessException(BusinessError.COUPON_NOT_USED);
        }

        this.used = false;
    }

}
