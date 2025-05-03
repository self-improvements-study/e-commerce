package kr.hhplus.be.server.presentation.order;

import kr.hhplus.be.server.domain.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@RequiredArgsConstructor
@Component
public class OrderScheduler {

    private final OrderService orderService;


    public void expireOrders() {
        orderService.expireOrders();
    }
}
