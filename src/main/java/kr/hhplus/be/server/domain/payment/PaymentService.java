package kr.hhplus.be.server.domain.payment;

import kr.hhplus.be.server.common.exception.BusinessError;
import kr.hhplus.be.server.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    private final PaymentEventPublisher paymentEventPublisher;

    /**
     * 결제 정보를 저장하고 저장된 결제 정보를 요약 형태로 반환합니다.
     *
     * @param command 결제에 필요한 주문 ID와 금액 정보를 담은 커맨드 객체
     * @return 저장된 결제 정보를 요약한 PaymentSummary DTO
     */
    @Transactional
    public PaymentInfo.PaymentSummary payment(PaymentCommand.Payment command) {

        // OptionStock 리스트 변환
        List<PaymentEvent.CreatePayment.OptionStock> optionStockList = command.getOptionStockList().stream()
                .map(optionStock -> PaymentEvent.CreatePayment.OptionStock.of(optionStock.getOptionId(), optionStock.getQuantity()))
                .toList();

        // ProductSignal 리스트 변환
        List<PaymentEvent.CreatePayment.ProductSignal> productSignalList = command.getProductSignalList().stream()
                .map(productSignal -> PaymentEvent.CreatePayment.ProductSignal
                        .of(productSignal.getProductId(), productSignal.getDate(), productSignal.getName(), productSignal.getQuantity()))
                .toList();

        // UserCoupon 리스트 변환
        List<PaymentEvent.CreatePayment.UserCoupon> userCouponList = command.getUserCouponList().stream()
                .map(userCoupon -> PaymentEvent.CreatePayment.UserCoupon.of(userCoupon.getUserCouponId()))
                .toList();

        // 이벤트 발행
        PaymentEvent.CreatePayment event = PaymentEvent.CreatePayment.from(
                command.getUserId(),
                command.getOrderId(),
                command.getAmount(),
                optionStockList,
                productSignalList,
                userCouponList
        );

        paymentEventPublisher.publish(event);

        if (command.getAmount() < 0) {
            throw new BusinessException(BusinessError.PAYMENT_AMOUNT_DECREASE_TOO_SMALL);
        }

        // 1. 결제 엔티티 생성
        Payment entity = Payment.toEntity(command.getOrderId(), command.getAmount());

        // 2. 결제 정보 저장
        Payment savePayment = paymentRepository.save(entity);

        // 3. 저장된 결제 정보를 DTO 형태로 반환
        return PaymentInfo.PaymentSummary.from(savePayment);
    }

    /**
     * 주문 ID를 기반으로 결제 내역을 조회합니다.
     *
     * @param orderId 결제 내역을 조회할 주문 ID
     * @return 결제 정보를 요약한 PaymentSummary DTO
     * @throws BusinessException 결제 정보가 존재하지 않을 경우 예외 발생
     */
    @Transactional
    public PaymentInfo.PaymentSummary paymentHistory(long orderId) {

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(BusinessError.PAYMENT_NOT_FOUND));

        return PaymentInfo.PaymentSummary.from(payment);
    }


}
