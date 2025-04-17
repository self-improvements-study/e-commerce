package kr.hhplus.be.server.domain.coupon;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;

public interface CouponQuery {

    record OwnedCoupon(
            long userCouponId,
            long couponId,
            long userId,
            String couponName,
            long discount,
            LocalDateTime startedDate,
            LocalDateTime endedDate,
            boolean used

    ) {
        @QueryProjection
        public OwnedCoupon {}

        public CouponInfo.OwnedCoupon to() {
            return CouponInfo.OwnedCoupon.builder()
                    .userCouponId(userCouponId)
                    .couponId(couponId)
                    .userId(userId)
                    .couponName(couponName)
                    .discount(discount)
                    .startedDate(startedDate)
                    .endedDate(endedDate)
                    .used(used)
                    .build();
        }
    }
}
