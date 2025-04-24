package kr.hhplus.be.server.domain.payment;

import kr.hhplus.be.server.common.exception.BusinessError;
import kr.hhplus.be.server.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    /**
     * 결제 정보를 저장하고 저장된 결제 정보를 요약 형태로 반환합니다.
     *
     * @param command 결제에 필요한 주문 ID와 금액 정보를 담은 커맨드 객체
     * @return 저장된 결제 정보를 요약한 PaymentSummary DTO
     */
    @Transactional
    public PaymentInfo.PaymentSummary payment(PaymentCommand.Payment command) {

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
