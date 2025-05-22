package kr.hhplus.be.server.domain.order;

public interface OrderEventPublisher {

    void publish(OrderEvent.Send event);

}
