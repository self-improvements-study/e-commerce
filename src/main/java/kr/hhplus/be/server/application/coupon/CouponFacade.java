package kr.hhplus.be.server.application.coupon;

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

    @Transactional(readOnly = true)
    public List<CouponResult.OwnedCoupon> getUserCoupons(Long userId) {
        List<CouponInfo.OwnedCoupon> infos = couponService.findUserCoupons(userId);
        return infos.stream()
                .map(CouponResult.OwnedCoupon::from)
                .toList();
    }
}
