package kr.hhplus.be.server.domain.order;

public interface OrderExternalClient {

    void sendOrder(OrderEvent.Send event);

}
