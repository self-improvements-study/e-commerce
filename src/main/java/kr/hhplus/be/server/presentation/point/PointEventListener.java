package kr.hhplus.be.server.presentation.point;

import kr.hhplus.be.server.domain.payment.PaymentEvent;
import kr.hhplus.be.server.domain.point.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PointEventListener {

    private final PointService pointService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void usePoint(PaymentEvent.CreatePayment event) {
        pointService.decrease(event.getUserId(), event.getAmount());
    }

}
