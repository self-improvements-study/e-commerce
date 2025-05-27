package kr.hhplus.be.server.presentation.coupon;

import kr.hhplus.be.server.common.event.EventFlowManager;
import kr.hhplus.be.server.common.event.EventFlowState;
import kr.hhplus.be.server.common.event.OrderEventFlow;
import kr.hhplus.be.server.domain.coupon.CouponCommand;
import kr.hhplus.be.server.domain.coupon.CouponService;
import kr.hhplus.be.server.domain.order.OrderCreatedEvent;
import kr.hhplus.be.server.domain.order.OrderEvent;
import kr.hhplus.be.server.domain.payment.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CouponEventListener {

    private final CouponService couponService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void useCoupon(OrderEvent.CreateOrder event) {

        List<Long> userCouponIds = event.getOrderItem().stream()
                .map(OrderEvent.CreateOrder.Item::getUserCouponId)
                .filter(Objects::nonNull)
                .toList();

        couponService.use(CouponCommand.Use.of(userCouponIds));

    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void cancelCoupon(PaymentEvent.CreatePayment event) {

        List<Long> userCouponIds = event.getUserCouponList().stream()
                .map(PaymentEvent.CreatePayment.UserCoupon::getUserCouponId)
                .filter(Objects::nonNull)
                .toList();

        couponService.cancel(CouponCommand.Cancel.of(userCouponIds));

    }

}
