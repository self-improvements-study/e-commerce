package kr.hhplus.be.server.domain.payment;


public interface PaymentEventPublisher {

    void publish(PaymentEvent.CreatePayment event);

}
