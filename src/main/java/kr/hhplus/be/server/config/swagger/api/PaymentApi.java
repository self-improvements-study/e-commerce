package kr.hhplus.be.server.config.swagger.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.presentation.payment.PaymentRequest;
import kr.hhplus.be.server.presentation.payment.PaymentResponse;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Payment", description = "결제 관련 API")
public interface PaymentApi {

    @Operation(summary = "주문 결제", description = "주문을 결제합니다.")
    PaymentResponse.Create createPayment(@RequestBody PaymentRequest.Create request);
}