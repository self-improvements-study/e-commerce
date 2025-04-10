package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.common.exception.BusinessError;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.coupon.CouponCommand;
import kr.hhplus.be.server.domain.coupon.CouponService;
import kr.hhplus.be.server.domain.order.OrderInfo;
import kr.hhplus.be.server.domain.order.OrderService;
import kr.hhplus.be.server.domain.payment.PaymentCommand;
import kr.hhplus.be.server.domain.payment.PaymentInfo;
import kr.hhplus.be.server.domain.payment.PaymentService;
import kr.hhplus.be.server.domain.point.PointInfo;
import kr.hhplus.be.server.domain.point.PointService;
import kr.hhplus.be.server.domain.product.ProductCommand;
import kr.hhplus.be.server.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@RequiredArgsConstructor
@Component
public class PaymentFacade {

    private final PaymentService paymentService;

    private final OrderService orderService;

    private final PointService pointService;

    private final ProductService productService;

    private final CouponService couponService;

    /**
     * 사용자 결제 요청을 처리합니다.
     *
     * @param userId 사용자 ID
     * @param orderId 주문 ID
     * @return 결제 성공 시 결제 요약 정보 반환
     * @throws BusinessException 결제 실패 시 예외 발생
     */
    public PaymentResult.PaymentSummary payment(long userId, long orderId) {
        // 1. 주문 조회
        OrderInfo.OrderHistory orderInfo = orderService.findOrderByOrderId(orderId);

        // 2. 주문 아이템에서 쿠폰 ID 및 옵션 재고 정보 추출
        List<Long> userCouponIds = orderInfo.getOrderItems().stream()
                .map(OrderInfo.OrderItemDetail::getUserCouponId)
                .toList();

        List<ProductCommand.OptionStock> optionStocks = orderInfo.getOrderItems().stream()
                .map(item -> ProductCommand.OptionStock.builder()
                        .optionId(item.getOptionId())
                        .quantity(item.getQuantity())
                        .build())
                .toList();

        // 3. 포인트 잔액 조회
        PointInfo.Balance pointInfo = pointService.getBalance(userId);

        try {
            // 4. 결제 처리
            PaymentInfo.PaymentSummary paymentInfo = paymentService.payment(
                    PaymentCommand.Payment.of(orderInfo.getOrderId(), orderInfo.getTotalAmount())
            );

            // 5. 포인트 차감
            pointService.decrease(userId, orderInfo.getTotalAmount());

            // 6. 쿠폰 사용
            if (!CollectionUtils.isEmpty(userCouponIds)) {
                couponService.use(CouponCommand.Use.of(userCouponIds));
            }

            // 7. 주문 상태 변경
            orderService.success(orderId);

            // 8. 결제 성공 결과 반환
            return PaymentResult.PaymentSummary.from(paymentInfo);

        } catch (Exception e) {
            // 9. 결제 실패 시 보상 로직
            // 9-1. 재고 복구
            productService.increaseStockQuantity(ProductCommand.IncreaseStock.of(optionStocks));

            // 9-2. 주문 상태 변경
            orderService.cancel(orderId);

            // 10. 결제 실패 예외 발생
            throw new BusinessException(BusinessError.PAYMENT_FAILED);
        }
    }
}
