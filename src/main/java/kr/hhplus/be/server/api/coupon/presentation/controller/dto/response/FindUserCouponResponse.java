package kr.hhplus.be.server.api.coupon.presentation.controller.dto.response;

import java.time.LocalDateTime;

public record FindUserCouponResponse(
        Long userCouponId,
        Long couponId,
        String couponName,
        Long discount,
        LocalDateTime startedDate,
        LocalDateTime endedDate
) {
}
