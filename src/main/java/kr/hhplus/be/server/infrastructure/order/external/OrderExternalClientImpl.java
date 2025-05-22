package kr.hhplus.be.server.infrastructure.order.external;

import kr.hhplus.be.server.domain.order.OrderExternalClient;
import kr.hhplus.be.server.domain.order.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderExternalClientImpl implements OrderExternalClient {

    @Override
    public void sendOrder(OrderEvent.Send event) {
        log.info("send order: {}", event);
    }

}
