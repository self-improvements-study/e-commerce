package kr.hhplus.be.server.presentation.order;

import kr.hhplus.be.server.domain.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@RequiredArgsConstructor
@Component
public class OrderScheduler {

    private final OrderService orderService;


    @Scheduled(fixedRate = 120000)
    public void expireOrders() {
        orderService.expireOrders();
    }
}
