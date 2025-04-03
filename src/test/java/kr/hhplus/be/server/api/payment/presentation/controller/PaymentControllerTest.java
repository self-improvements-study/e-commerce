package kr.hhplus.be.server.api.payment.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.api.payment.presentation.controller.dto.request.CreatePaymentRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("주문 결제")
    void createPayment() throws Exception {
        // given
        CreatePaymentRequest request = new CreatePaymentRequest(12345L);
        String content = new ObjectMapper().writeValueAsString(request);

        mockMvc
                // when
                .perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
                )
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo(200)))
                .andExpect(jsonPath("$.message", equalTo("성공")))
                .andExpect(jsonPath("$.data.orderId", equalTo(12345)))
                .andExpect(jsonPath("$.data.paymentId", notNullValue()))
                .andExpect(jsonPath("$.data.amount", equalTo(30000)))
                .andExpect(jsonPath("$.data.paymentDate", notNullValue()));
    }
}
