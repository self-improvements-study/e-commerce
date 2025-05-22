package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.order.OrderInfo;
import kr.hhplus.be.server.domain.order.OrderService;
import kr.hhplus.be.server.domain.payment.PaymentCommand;
import kr.hhplus.be.server.domain.payment.PaymentInfo;
import kr.hhplus.be.server.domain.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Component
public class PaymentFacade {

    private final PaymentService paymentService;

    private final OrderService orderService;

    /**
     * 사용자 결제 요청을 처리합니다.
     *
     * @param userId  사용자 ID
     * @param orderId 주문 ID
     * @return 결제 성공 시 결제 요약 정보 반환
     * @throws BusinessException 결제 실패 시 예외 발생
     */
    @Transactional
    public PaymentResult.PaymentSummary payment(long userId, long orderId) {
        // 1. 주문 조회
        OrderInfo.OrderHistory orderInfo = orderService.findOrderByOrderId(orderId);

        // 2. 주문 아이템에서 쿠폰 ID 및 옵션 재고 정보 추출
        List<PaymentCommand.Payment.UserCoupon> userCouponList = orderInfo.getOrderItems().stream()
                .map(item -> item.getUserCouponId() != null ? PaymentCommand.Payment.UserCoupon.of(item.getUserCouponId()) : null)
                .filter(Objects::nonNull)
                .toList();

        List<PaymentCommand.Payment.ProductSignal> productSignalList = orderInfo.getOrderItems().stream()
                .map(item -> PaymentCommand.Payment.ProductSignal
                        .of(item.getProductId(), item.getOrderDate(), item.getProductName(), item.getQuantity()))
                .toList();

        List<PaymentCommand.Payment.OptionStock> optionStockList = orderInfo.getOrderItems().stream()
                .map(item -> PaymentCommand.Payment.OptionStock.of(item.getOptionId(), item.getQuantity()))
                .toList();

        // 3. 결제 처리
        PaymentInfo.PaymentSummary paymentInfo = paymentService.payment(
                PaymentCommand.Payment.of(
                        userId,
                        orderInfo.getOrderId(),
                        orderInfo.getTotalAmount(),
                        optionStockList,
                        productSignalList,
                        userCouponList
                        )
        );

        // 4. 결제 성공 결과 반환
        return PaymentResult.PaymentSummary.from(paymentInfo);
    }
}
