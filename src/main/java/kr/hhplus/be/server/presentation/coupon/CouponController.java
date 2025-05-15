package kr.hhplus.be.server.presentation.coupon;

import kr.hhplus.be.server.application.coupon.CouponFacade;
import kr.hhplus.be.server.application.coupon.CouponResult;
import kr.hhplus.be.server.config.swagger.api.CouponApi;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/coupons")
public class CouponController implements CouponApi {

    private final CouponFacade couponFacade;

    // 선착순 쿠폰 발급
    @PostMapping
    public CouponResponse.CreateUserCoupon createUserCoupon(
            @RequestBody CouponRequest.Issue request
    ) {
        CouponResult.IssuedCoupon issuedCouponDetail = couponFacade.issueCoupon(request.toCriteria());
        return CouponResponse.CreateUserCoupon.from(issuedCouponDetail);
    }

    @PostMapping("/enqueue")
    public CouponResponse.IssuedCoupon addCouponRequest(
            @RequestBody CouponRequest.Issue request
    ) {
        return CouponResponse.IssuedCoupon.from(couponFacade.addCouponToQueue(request.toCriteria()));
    }

    // 유저 쿠폰 조회
    @GetMapping("{userId}")
    public List<CouponResponse.FindUserCoupon> findUserCoupons(
            @PathVariable Long userId
    ) {
        List<CouponResult.OwnedCoupon> userCoupons = couponFacade.getUserCoupons(userId);
        return userCoupons.stream()
                .map(CouponResponse.FindUserCoupon::from)
                .toList();
    }

}
