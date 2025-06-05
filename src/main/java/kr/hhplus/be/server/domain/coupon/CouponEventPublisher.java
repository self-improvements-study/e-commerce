package kr.hhplus.be.server.domain.coupon;

public interface CouponEventPublisher {

    void publish(CouponEvent.issued event);

}
