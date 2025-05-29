package kr.hhplus.be.server.presentation.order;

import kr.hhplus.be.server.domain.order.OrderEvent;
import kr.hhplus.be.server.domain.order.OrderExternalClient;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderExternalClient orderExternalClient;

    @KafkaListener(topics = "order.v1.completed")
    public void sendOrderDataByKafka(OrderEvent.OrderCompleted event, Acknowledgment ack) {
        // 외부 플랫폼 데이터 전송
        orderExternalClient.sendOrder(event);

        ack.acknowledge();
    }

}
