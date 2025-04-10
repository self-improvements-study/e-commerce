package kr.hhplus.be.server.domain.coupon;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.common.entity.AuditableEntity;
import lombok.*;

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
    @Column(name = "used", nullable = false)
    private boolean used;

    /**
     * 쿠폰 사용 처리
     */
    public void use() {
        if (this.used) {
            throw new IllegalStateException("이미 사용한 쿠폰입니다.");
        }
        this.used = true;
    }

    /**
     * 쿠폰 미사용 처리
     */
    public void cancel() {
        if (!this.used) {
            throw new IllegalStateException("사용되지 않은 쿠폰입니다.");
        }

        this.used = false;
    }

}
