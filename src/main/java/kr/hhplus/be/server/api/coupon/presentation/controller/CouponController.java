package kr.hhplus.be.server.api.coupon.presentation.controller;

import kr.hhplus.be.server.api.coupon.presentation.controller.dto.request.CreateUserCouponRequest;
import kr.hhplus.be.server.api.coupon.presentation.controller.dto.response.CreateUserCouponResponse;
import kr.hhplus.be.server.api.coupon.presentation.controller.dto.response.FindUserCouponResponse;
import kr.hhplus.be.server.api.order.presentation.OrderController;
import kr.hhplus.be.server.common.response.CommonResponse;
import kr.hhplus.be.server.config.swagger.api.CouponApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/coupons")
public class CouponController implements CouponApi {
    private static final Logger logger = LoggerFactory.getLogger(CouponController.class);

    // 쿠폰 발급
    @PostMapping
    public CommonResponse<CreateUserCouponResponse> createUserCoupon(
            @RequestBody CreateUserCouponRequest request
    ) {
        CreateUserCouponResponse data = new CreateUserCouponResponse(
                23L,
                36L,
                "선착순 쿠폰",
                3000L,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(6)
        );
        logger.info("컨트롤러 진입 - 쿠폰 발급 요청");

        return CommonResponse.success(data);
    }

    // 유저 쿠폰 조회
    @GetMapping("{userId}")
    public CommonResponse<List<FindUserCouponResponse>> findUserCoupons(
            @PathVariable Long userId
    ) {
        List<FindUserCouponResponse> data = List.of(new FindUserCouponResponse(
                23L,
                36L,
                "선착순 쿠폰",
                3000L,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(6)
        ));

        return CommonResponse.success(data);
    }

}
