package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.domain.coupon.CouponCommand;
import kr.hhplus.be.server.domain.coupon.CouponInfo;
import kr.hhplus.be.server.domain.coupon.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class CouponFacade {

    private final CouponService couponService;

    public CouponResult.IssuedCoupon issueCoupon(CouponCriteria.IssuedCoupon issuedCoupon) {
        CouponInfo.IssuedCoupon info = couponService.issueCoupon(issuedCoupon.toCommand());
        return CouponResult.IssuedCoupon.from(info);
    }

    public CouponResult.CouponActivation addCouponToQueue(CouponCriteria.IssuedCoupon issuedCoupon) {
        CouponInfo.CouponActivation info = couponService.addCouponToQueue(issuedCoupon.toCommand());

        return CouponResult.CouponActivation.from(info);
    }

    @Transactional
    public void issuedCoupon() {
        // 1. 발급 가능한 상태인 쿠폰들을 조회합니다.
        List<CouponInfo.AvailableCoupon> byCouponStatus = couponService.findByCouponStatus();

        // 2. 발급 가능한 쿠폰들에 대해 반복 처리를 시작합니다.
        byCouponStatus.
                stream()
                .map(v -> CouponCommand.Issued.of(v.getCouponId(), v.getQuantity(), v.getStatus()))
                .forEach(couponService::issuedCoupon);
    }

    @Transactional(readOnly = true)
    public List<CouponResult.OwnedCoupon> getUserCoupons(Long userId) {
        List<CouponInfo.OwnedCoupon> infos = couponService.findUserCoupons(userId);
        return infos.stream()
                .map(CouponResult.OwnedCoupon::from)
                .toList();
    }
}
