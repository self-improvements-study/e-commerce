package kr.hhplus.be.server.presentation.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.application.payment.PaymentFacade;
import kr.hhplus.be.server.application.payment.PaymentResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentFacade paymentFacade;


    @Test
    @DisplayName("주문 결제")
    void createPayment() throws Exception {
        // given
        long userId = 100L;
        long orderId = 123L;

        PaymentRequest.Create request = new PaymentRequest.Create(userId, orderId);
        String content = new ObjectMapper().writeValueAsString(request);

        PaymentResult.PaymentSummary paymentSummary = PaymentResult.PaymentSummary.builder()
                .orderId(orderId)
                .paymentId(456L)
                .amount(5000L)
                .paymentDate(LocalDateTime.now())
                .build();

        when(paymentFacade.payment(anyLong(), anyLong())).thenReturn(paymentSummary);

        // when & then
        mockMvc.perform(post("/api/v1/payments")
                        .contentType("application/json")
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.orderId").value(orderId))
                .andExpect(jsonPath("$.data.paymentId").value(456))
                .andExpect(jsonPath("$.data.amount").value(5000))
                .andExpect(jsonPath("$.data.paymentDate").exists());
    }
}
