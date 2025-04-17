package kr.hhplus.be.server.presentation.coupon;

import kr.hhplus.be.server.application.coupon.CouponResult;

import java.time.LocalDateTime;

public record CouponResponse() {

    public record CreateUserCoupon(
            long userCouponId,
            long couponId,
            String couponName,
            long discount,
            LocalDateTime startedDate,
            LocalDateTime endedDate
    ) {
        public static CreateUserCoupon from(CouponResult.IssuedCoupon issuedCoupon) {
            return new CreateUserCoupon(
                    issuedCoupon.getUserCouponId(),
                    issuedCoupon.getCouponId(),
                    issuedCoupon.getCouponName(),
                    issuedCoupon.getDiscount(),
                    issuedCoupon.getStartedDate(),
                    issuedCoupon.getEndedDate()
            );
        }
    }

    public record FindUserCoupon(
            long userCouponId,
            long couponId,
            long userId,
            String couponName,
            long discount,
            LocalDateTime startedDate,
            LocalDateTime endedDate,
            boolean used
    ) {
        public static FindUserCoupon from(CouponResult.OwnedCoupon ownedCoupon) {
            return new FindUserCoupon(
                    ownedCoupon.getUserCouponId(),
                    ownedCoupon.getCouponId(),
                    ownedCoupon.getUserId(),
                    ownedCoupon.getCouponName(),
                    ownedCoupon.getDiscount(),
                    ownedCoupon.getStartedDate(),
                    ownedCoupon.getEndedDate(),
                    ownedCoupon.isUsed()
            );
        }
    }
}