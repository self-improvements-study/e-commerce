package kr.hhplus.be.server.domain.coupon;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CouponCommand {

    @Getter
    @Builder
    public static class IssuedCouponBatch {

        Long couponId;
        List<IssuedCoupon> issuedCouponDetail;

        public static IssuedCouponBatch from(Long couponId, List<CouponEvent.issued>  detail) {
            return IssuedCouponBatch.builder()
                    .couponId(couponId)
                    .issuedCouponDetail(detail
                            .stream()
                            .map(v -> IssuedCoupon.from(v.getCouponId(), v.getUserId()))
                            .toList())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class IssuedCoupon {
        long userId;
        long couponId;

        public static IssuedCoupon from(Long couponId, Long userId) {
            return IssuedCoupon.builder()
                    .userId(userId)
                    .couponId(couponId)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class Issued {
        Long couponId;
        Long quantity;
        Coupon.Status status;

        public static Issued of(Long couponId, Long quantity, Coupon.Status status) {
            return Issued.builder()
                    .couponId(couponId)
                    .quantity(quantity)
                    .status(status)
                    .build();
        }

    }

    @Getter
    @Builder
    public static class Use {
        private List<OwnedCoupon> ownedCoupons;

        public static Use of(List<Long> userCouponIds) {
            List<OwnedCoupon> ownedCoupons = userCouponIds.stream()
                    .map(id -> OwnedCoupon.builder()
                            .userCouponId(id)
                            .build())
                    .toList();

            return Use.builder()
                    .ownedCoupons(ownedCoupons)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class Cancel {
        private List<OwnedCoupon> ownedCoupons;

        public static Cancel of(List<Long> userCouponIds) {
            List<OwnedCoupon> ownedCoupons = userCouponIds.stream()
                    .map(id -> OwnedCoupon.builder()
                            .userCouponId(id)
                            .build())
                    .toList();

            return Cancel.builder()
                    .ownedCoupons(ownedCoupons)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class OwnedCoupon {
        private long userCouponId;
    }

    @Getter
    @Builder
    public static class FindUserCoupons {
        private List<Long> userCouponIds;

        public static FindUserCoupons of(List<Long> userCouponIds) {
            return FindUserCoupons.builder()
                    .userCouponIds(userCouponIds)
                    .build();
        }
    }

}
