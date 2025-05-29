package kr.hhplus.be.server.infrastructure.order.event;

import kr.hhplus.be.server.domain.order.OrderCreatedEvent;
import kr.hhplus.be.server.domain.order.OrderEvent;
import kr.hhplus.be.server.domain.order.OrderEventPublisher;
import kr.hhplus.be.server.domain.order.OrderPaymentWaitedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventSpringPublisher implements OrderEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publish(OrderEvent.Send event) {
        eventPublisher.publishEvent(event);
    }
}
