package kr.hhplus.be.server.presentation.coupon;

import kr.hhplus.be.server.domain.coupon.CouponCommand;
import kr.hhplus.be.server.domain.coupon.CouponEvent;
import kr.hhplus.be.server.domain.coupon.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.List;

import static kr.hhplus.be.server.domain.coupon.CouponCommand.IssuedCouponBatch.*;

@Component
@RequiredArgsConstructor
public class CouponEventListener {

    private final CouponService couponService;

    @KafkaListener(topics = "coupon.v1.issue", batch = "true")
    public void consume(List<CouponEvent.issued> event, Acknowledgment ack,
                        @Header(KafkaHeaders.RECEIVED_KEY) List<String> key
    ) {

        String couponId = key.get(0);
        CouponCommand.IssuedCouponBatch command = from(Long.valueOf(couponId), event);

        couponService.issueCoupon(command);

        ack.acknowledge();

    }

}
