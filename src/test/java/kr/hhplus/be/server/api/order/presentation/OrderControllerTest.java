package kr.hhplus.be.server.api.order.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.api.order.presentation.controller.dto.request.CreateOrderRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("상품 주문")
    void createOrder() throws Exception {
        // given
        long userId = 1L;
        CreateOrderRequest request = new CreateOrderRequest(List.of());
        String content = new ObjectMapper().writeValueAsString(request);

        mockMvc
                // when
                .perform(post("/api/v1/orders/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
                )
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo(200)))
                .andExpect(jsonPath("$.message", equalTo("성공")))
                .andExpect(jsonPath("$.data.orderId", notNullValue()))
                .andExpect(jsonPath("$.data.totalAmount", equalTo(50000)))
                .andExpect(jsonPath("$.data.status", equalTo("PAYMENT_WAITING")));
    }

    @Test
    @DisplayName("유저 주문 내역 조회")
    void getUserOrders() throws Exception {
        // given
        long userId = 1L;

        mockMvc
                // when
                .perform(get("/api/v1/orders/users/{userId}", userId))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo(200)))
                .andExpect(jsonPath("$.message", equalTo("성공")))
                .andExpect(jsonPath("$.data[0].orderId", equalTo(12345)))
                .andExpect(jsonPath("$.data[0].status", equalTo("SUCCESS")))
                .andExpect(jsonPath("$.data[0].totalAmount", equalTo(30000)))
                .andExpect(jsonPath("$.data[0].orderItems", notNullValue()));
    }

    @Test
    @DisplayName("주문 상세 내역 조회")
    void getOrderDetail() throws Exception {
        // given
        long orderId = 12345L;

        mockMvc
                // when
                .perform(get("/api/v1/orders/{orderId}", orderId))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo(200)))
                .andExpect(jsonPath("$.message", equalTo("성공")))
                .andExpect(jsonPath("$.data.orderId", equalTo(12345)))
                .andExpect(jsonPath("$.data.status", equalTo("SUCCESS")))
                .andExpect(jsonPath("$.data.totalAmount", equalTo(30000)))
                .andExpect(jsonPath("$.data.orderItems", notNullValue()))
                .andExpect(jsonPath("$.data.payment.paymentId", equalTo(123)))
                .andExpect(jsonPath("$.data.payment.amount", equalTo(30000)))
                .andExpect(jsonPath("$.data.payment.paymentDate", notNullValue()));
    }
}
