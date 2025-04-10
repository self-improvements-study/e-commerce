package kr.hhplus.be.server.presentation.coupon;

import kr.hhplus.be.server.application.coupon.CouponCriteria;

public record CouponRequest() {

    public record Issue(
            long userId,
            long couponId
    ) {
        public CouponCriteria.IssuedCoupon toCriteria() {
            return CouponCriteria.IssuedCoupon.builder()
                    .userId(this.userId)
                    .couponId(this.couponId)
                    .build();
        }
    }

}