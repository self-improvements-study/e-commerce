package kr.hhplus.be.server.api.payment.presentation.controller;

import kr.hhplus.be.server.api.payment.presentation.controller.dto.request.CreatePaymentRequest;
import kr.hhplus.be.server.api.payment.presentation.controller.dto.response.CreatePaymentResponse;
import kr.hhplus.be.server.common.response.CommonResponse;
import kr.hhplus.be.server.config.swagger.api.PaymentApi;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController implements PaymentApi {

    // 주문 결제
    @PostMapping
    public CommonResponse<CreatePaymentResponse> createPayment(
            @RequestBody CreatePaymentRequest request
    ) {
        CreatePaymentResponse data = new CreatePaymentResponse(
                request.orderId(),
                98765L,
                30000L,
                LocalDateTime.now()
        );

        return CommonResponse.success(data);
    }

}
