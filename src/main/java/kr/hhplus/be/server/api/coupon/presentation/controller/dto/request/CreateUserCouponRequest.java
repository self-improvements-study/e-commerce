package kr.hhplus.be.server.api.coupon.presentation.controller.dto.request;

public record CreateUserCouponRequest(
        Long userId,
        Long couponId
) {
}
