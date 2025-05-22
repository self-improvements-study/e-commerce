package kr.hhplus.be.server.presentation.coupon;

import kr.hhplus.be.server.application.coupon.CouponFacade;
import kr.hhplus.be.server.domain.coupon.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CouponScheduler {

    private final CouponFacade couponFacade;

    private final CouponService couponService;

    @Scheduled(cron = "0 0 0 * * ?")  // 매일 자정에 실행
    public void expireCoupons() {
        couponService.expireCoupons();
    }

    @Scheduled(fixedRate = 10000) // 10초마다 실행
    public void issueCoupons() {
        couponFacade.issuedCoupon();
    }
}