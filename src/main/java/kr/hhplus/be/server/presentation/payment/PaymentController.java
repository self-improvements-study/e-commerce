package kr.hhplus.be.server.presentation.payment;

import kr.hhplus.be.server.application.payment.PaymentFacade;
import kr.hhplus.be.server.application.payment.PaymentResult;
import kr.hhplus.be.server.config.swagger.api.PaymentApi;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController implements PaymentApi {

    private final PaymentFacade paymentFacade;

    // 주문 결제
    @PostMapping
    public PaymentResponse.Create createPayment(
            @RequestBody PaymentRequest.Create request
    ) {
        PaymentResult.PaymentSummary result =
                paymentFacade.payment(request.getUserId(), request.getOrderId());
        return PaymentResponse.Create.from(result);
    }
}
