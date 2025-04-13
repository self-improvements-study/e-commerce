package kr.hhplus.be.server.config.swagger.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.presentation.coupon.CouponRequest;
import kr.hhplus.be.server.presentation.coupon.CouponResponse;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Tag(name = "Coupon", description = "쿠폰 관련 API")
public interface CouponApi {

    @Operation(summary = "쿠폰 발급", description = "새로운 쿠폰을 발급합니다.")
    CouponResponse.CreateUserCoupon createUserCoupon(CouponRequest.Issue request);

    @Operation(summary = "유저 쿠폰 조회", description = "특정 유저의 쿠폰 목록을 조회합니다.")
    List<CouponResponse.FindUserCoupon> findUserCoupons(
            @Parameter(description = "유저 ID", example = "1")@PathVariable Long userId);
}
