package kr.hhplus.be.server.presentation.coupon;

import kr.hhplus.be.server.domain.coupon.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CouponScheduler {

    private final CouponService couponService;

    public void expireCoupons() {
        couponService.expireCoupons();
    }
}