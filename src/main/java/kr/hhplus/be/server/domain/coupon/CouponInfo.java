package kr.hhplus.be.server.domain.coupon;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CouponInfo {

    @Getter
    @Builder
    public static class IssuedCoupon {
        private long userCouponId;
        private long couponId;
        private String couponName;
        private long discount;
        private LocalDateTime startedDate;
        private LocalDateTime endedDate;

        public static IssuedCoupon from(UserCoupon userCoupon, Coupon coupon) {
            return IssuedCoupon.builder()
                    .userCouponId(userCoupon.getId())
                    .couponId(coupon.getId())
                    .couponName(coupon.getCouponName())
                    .discount(coupon.getDiscount())
                    .startedDate(coupon.getStartedDate())
                    .endedDate(coupon.getEndedDate())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class CouponActivation {
        private long userId;
        private long couponId;

        public static CouponActivation from(long userId, long couponId) {
            return CouponActivation.builder()
                    .userId(userId)
                    .couponId(couponId)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class OwnedCoupon {
        private long userCouponId;
        private long couponId;
        private long userId;
        private String couponName;
        private long discount;
        private LocalDateTime startedDate;
        private LocalDateTime endedDate;
        private boolean used;

    }

    @Getter
    @Builder
    public static class AvailableCoupon {
        private Long couponId;
        private Long quantity;
        private Coupon.Status status;

        public static AvailableCoupon from(Coupon coupon) {
            return AvailableCoupon.builder()
                    .couponId(coupon.getId())
                    .quantity(coupon.getQuantity())
                    .status(coupon.getStatus())
                    .build();
        }

    }
}
