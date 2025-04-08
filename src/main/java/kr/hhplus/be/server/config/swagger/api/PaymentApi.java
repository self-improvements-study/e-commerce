package kr.hhplus.be.server.config.swagger.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.api.payment.presentation.controller.dto.request.CreatePaymentRequest;
import kr.hhplus.be.server.api.payment.presentation.controller.dto.response.CreatePaymentResponse;
import kr.hhplus.be.server.common.response.CommonResponse;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Payment", description = "결제 관련 API")
public interface PaymentApi {

    @Operation(summary = "주문 결제", description = "주문을 결제합니다.")
    CommonResponse<CreatePaymentResponse> createPayment(@RequestBody CreatePaymentRequest request);
}