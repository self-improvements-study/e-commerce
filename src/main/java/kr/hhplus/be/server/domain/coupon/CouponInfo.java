package kr.hhplus.be.server.domain.coupon;

import kr.hhplus.be.server.infrastructure.coupon.CouponQuery;
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
        private String couponName;
        private long discount;
        private LocalDateTime startedDate;
        private LocalDateTime endedDate;

        public static OwnedCoupon from(CouponQuery.OwnedCouponProjection projection) {
            return OwnedCoupon.builder()
                    .userCouponId(projection.getUserCouponId())
                    .couponId(projection.getCouponId())
                    .couponName(projection.getCouponName())
                    .discount(projection.getDiscount())
                    .startedDate(projection.getStartedDate())
                    .endedDate(projection.getEndedDate())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class Detail {
        private long couponId;
        private long userId;
        private long userCouponId;
        private LocalDateTime startedDate;
        private LocalDateTime endedDate;
        private boolean used;

        public static Detail from(CouponQuery.DetailProjection projection) {
            return Detail.builder()
                    .couponId(projection.getCouponId())
                    .userId(projection.getUserId())
                    .userCouponId(projection.getUserCouponId())
                    .startedDate(projection.getStartedDate())
                    .endedDate(projection.getEndedDate())
                    .used(projection.isUsed())
                    .build();
        }
    }

}
