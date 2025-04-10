package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.domain.coupon.CouponCommand;
import kr.hhplus.be.server.domain.coupon.CouponInfo;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CouponCriteria {

    @Getter
    @Builder
    public static class IssuedCoupon {
        private long userId;
        private long couponId;

        public CouponCommand.IssuedCoupon toCommand() {
            return CouponCommand.IssuedCoupon.builder()
                    .userId(this.userId)
                    .couponId(this.couponId)
                    .build();
        }
    }
}
