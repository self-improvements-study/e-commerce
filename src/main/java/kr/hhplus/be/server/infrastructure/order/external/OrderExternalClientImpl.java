package kr.hhplus.be.server.infrastructure.order.external;

import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderExternalClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderExternalClientImpl implements OrderExternalClient {

    @Async
    @Override
    public void sendOrder(Order order) {
        log.info("send order: {}", order);
    }
}
