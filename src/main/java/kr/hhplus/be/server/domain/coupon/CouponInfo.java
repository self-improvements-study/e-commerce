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
    }
