package kr.hhplus.be.server.presentation.order;

import kr.hhplus.be.server.application.order.OrderFacade;
import kr.hhplus.be.server.application.order.OrderResult;
import kr.hhplus.be.server.config.swagger.api.OrderApi;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController implements OrderApi {

    private final OrderFacade orderFacade;

    // 상품 주문
    @PostMapping
    public OrderResponse.Create createOrder(
            @RequestBody OrderRequest.Create request
    ) {
        OrderResult.OrderSummary result = orderFacade.order(request.toCriteria());
        return OrderResponse.Create.from(result);
    }

    // 주문 내역 조회
    @GetMapping("/users/{userId}")
    public List<OrderResponse.Find> getUserOrders(@PathVariable Long userId) {
        List<OrderResult.OrderHistory> orderHistories = orderFacade.getUserOrders(userId);

        return orderHistories.stream()
                .map(OrderResponse.Find::from)
                .collect(Collectors.toList());
    }

    // 주문 상세 내역 조회
    @GetMapping("/{orderId}")
    public OrderResponse.FindDetail getOrderDetail(@PathVariable Long orderId) {

        OrderResult.FindDetail result = orderFacade.getUserOrderDetail(orderId);

        return OrderResponse.FindDetail.from(result);
    }
}
