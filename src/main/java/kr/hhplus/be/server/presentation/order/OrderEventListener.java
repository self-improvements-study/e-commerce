package kr.hhplus.be.server.presentation.order;

import kr.hhplus.be.server.common.event.EventFlowManager;
import kr.hhplus.be.server.common.event.EventFlowState;
import kr.hhplus.be.server.common.event.OrderEventFlow;
import kr.hhplus.be.server.domain.coupon.CouponUseEvent;
import kr.hhplus.be.server.domain.order.OrderEvent;
import kr.hhplus.be.server.domain.order.OrderExternalClient;
import kr.hhplus.be.server.domain.order.OrderPaymentWaitedEvent;
import kr.hhplus.be.server.domain.order.OrderService;
import kr.hhplus.be.server.domain.product.StockDecreaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderExternalClient orderExternalClient;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendOrderData(OrderEvent.Send event) {
        // 외부 플랫폼 데이터 전송
        orderExternalClient.sendOrder(event);
    }

}
