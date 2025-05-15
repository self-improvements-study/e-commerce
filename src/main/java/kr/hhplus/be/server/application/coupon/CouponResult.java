package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.domain.coupon.CouponInfo;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CouponResult {

    @Getter
    @Builder
    public static class IssuedCoupon {
        private long userCouponId;
        private long couponId;
        private String couponName;
        private long discount;
        private LocalDateTime startedDate;
        private LocalDateTime endedDate;

        public static IssuedCoupon from(CouponInfo.IssuedCoupon info) {
            return IssuedCoupon.builder()
                    .userCouponId(info.getUserCouponId())
                    .couponId(info.getCouponId())
                    .couponName(info.getCouponName())
                    .discount(info.getDiscount())
                    .startedDate(info.getStartedDate())
                    .endedDate(info.getEndedDate())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class CouponActivation {
        private long userId;
        private long couponId;

        public static CouponActivation from(CouponInfo.CouponActivation info) {
            return CouponActivation.builder()
                    .userId(info.getUserId())
                    .couponId(info.getCouponId())
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

        public static OwnedCoupon from(CouponInfo.OwnedCoupon info) {
            return OwnedCoupon.builder()
                    .userCouponId(info.getUserCouponId())
                    .couponId(info.getCouponId())
                    .userId(info.getUserId())
                    .couponName(info.getCouponName())
                    .discount(info.getDiscount())
                    .startedDate(info.getStartedDate())
                    .endedDate(info.getEndedDate())
                    .used(info.isUsed())
                    .build();
        }
    }
}
