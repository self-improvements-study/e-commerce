package kr.hhplus.be.server.api.order.presentation;

import kr.hhplus.be.server.api.order.presentation.controller.dto.request.CreateOrderRequest;
import kr.hhplus.be.server.api.order.presentation.controller.dto.response.CreateOrderResponse;
import kr.hhplus.be.server.api.order.presentation.controller.dto.response.FindOrderDetailResponse;
import kr.hhplus.be.server.api.order.presentation.controller.dto.response.FindOrderResponse;
import kr.hhplus.be.server.common.response.CommonResponse;
import kr.hhplus.be.server.config.swagger.api.OrderApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController implements OrderApi {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    // 상품 주문
    @PostMapping("{userId}")
    public CommonResponse<CreateOrderResponse> createOrder(
            @PathVariable Long userId,
            @RequestBody CreateOrderRequest request
    ) {
        CreateOrderResponse data = new CreateOrderResponse(
                12345L,
                50000L,
                "PAYMENT_WAITING"
        );
        logger.info("컨트롤러 진입 - 주문 생성 요청");
        return CommonResponse.success(data);
    }

    // 주문 내역 조회
    @GetMapping("/users/{userId}")
    public CommonResponse<List<FindOrderResponse>> getUserOrders(@PathVariable Long userId) {
        List<FindOrderResponse> orders = List.of(
                new FindOrderResponse(
                        12345L,
                        "SUCCESS",
                        30000L,
                        List.of(
                                new FindOrderResponse.FindOrderItemResponse(
                                        101L,
                                        "스니커즈",
                                        "270mm",
                                        "화이트",
                                        2L,
                                        20000L
                                ),
                                new FindOrderResponse.FindOrderItemResponse(
                                        205L,
                                        "러닝화",
                                        "280mm",
                                        "블랙",
                                        1L,
                                        10000L
                                )
                        )
                )
        );

        return CommonResponse.success(orders);
    }

    // 주문 상세 내역 조회
    @GetMapping("/{orderId}")
    public CommonResponse<FindOrderDetailResponse> getOrderDetail(@PathVariable Long orderId) {
        FindOrderDetailResponse orderDetail = new FindOrderDetailResponse(
                12345L,
                "SUCCESS",
                30000L,
                List.of(
                        new FindOrderDetailResponse.FindOrderItemDetailResponse(
                                101L,
                                "스니커즈",
                                "270mm",
                                "화이트",
                                2L,
                                20000L
                        ),

                        new FindOrderDetailResponse.FindOrderItemDetailResponse(
                                205L,
                                "러닝화",
                                "280mm",
                                "블랙",
                                1L,
                                10000L
                        )
                ),
                new FindOrderDetailResponse.FindPaymentResponse(
                        123L
                        , 12345L
                        , 30000L
                        , LocalDateTime.of(2024, 4, 3, 12, 30, 45))
        );

        return CommonResponse.success(orderDetail);
    }


}
