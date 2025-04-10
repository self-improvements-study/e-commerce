package kr.hhplus.be.server.infrastructure.coupon;

import java.time.LocalDateTime;

public class CouponQuery {

    public interface DetailProjection {
        long getCouponId();
        long getUserId();
        long getUserCouponId();
        LocalDateTime getStartedDate();
        LocalDateTime getEndedDate();
        boolean isUsed();
    }

    public interface OwnedCouponProjection {
        long getUserCouponId();
        long getCouponId();
        String getCouponName();
        long getDiscount();
        LocalDateTime getStartedDate();
        LocalDateTime getEndedDate();
    }
}
