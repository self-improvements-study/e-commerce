package kr.hhplus.be.server.config.swagger.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.presentation.order.OrderRequest;
import kr.hhplus.be.server.presentation.order.OrderResponse;
import kr.hhplus.be.server.presentation.order.presentation.controller.dto.request.CreateOrderRequest;
import kr.hhplus.be.server.presentation.order.presentation.controller.dto.response.CreateOrderResponse;
import kr.hhplus.be.server.presentation.order.presentation.controller.dto.response.FindOrderDetailResponse;
import kr.hhplus.be.server.presentation.order.presentation.controller.dto.response.FindOrderResponse;
import kr.hhplus.be.server.presentation.common.response.CommonResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Order", description = "주문 관련 API")
public interface OrderApi {

    @Operation(summary = "상품 주문", description = "유저가 상품을 주문합니다.")
    OrderResponse.Create createOrder(
            @RequestBody OrderRequest.Create request
    );

    @Operation(summary = "주문 내역 조회", description = "유저의 주문 내역을 조회합니다.")
    List<OrderResponse.Find> getUserOrders(
            @Parameter(description = "유저 ID", example = "1") @PathVariable Long userId
    );

    @Operation(summary = "주문 상세 조회", description = "특정 주문의 상세 내역을 조회합니다.")
    OrderResponse.FindDetail getOrderDetail(
            @Parameter(description = "주문 ID", example = "1") @PathVariable Long orderId
    );
}
