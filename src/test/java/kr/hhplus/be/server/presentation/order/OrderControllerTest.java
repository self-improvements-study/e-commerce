package kr.hhplus.be.server.presentation.order;

import kr.hhplus.be.server.application.order.OrderFacade;
import kr.hhplus.be.server.application.order.OrderResult;
import kr.hhplus.be.server.domain.order.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderFacade orderFacade;


    @Test
    @DisplayName("상품 주문")
    void createOrder() throws Exception {
        // given
        OrderRequest.Create request = new OrderRequest.Create(100L, List.of(
                new OrderRequest.Create.CreateOrderItem(1L, 2, null)
        ));
        String content = objectMapper.writeValueAsString(request);

        OrderResult.OrderSummary orderSummary = OrderResult.OrderSummary.builder()
                .orderId(123L)
                .userId(100L)
                .orderDate(LocalDateTime.now())
                .status(Order.Status.PAYMENT_WAITING)
                .totalPrice(5000L)
                .orderItems(List.of(
                        OrderResult.OrderItemSummary.builder()
                                .optionId(1L)
                                .originalPrice(2500L)
                                .quantity(2)
                                .userCouponId(0)
                                .build()
                ))
                .build();

        when(orderFacade.order(any()))
                .thenReturn(orderSummary);

        // then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.orderId").value(123))
                .andExpect(jsonPath("$.data.userId").value(100))
                .andExpect(jsonPath("$.data.status").value("PAYMENT_WAITING"))
                .andExpect(jsonPath("$.data.totalPrice").value(5000))
                .andExpect(jsonPath("$.data.orderItems[0].optionId").value(1))
                .andExpect(jsonPath("$.data.orderItems[0].originalPrice").value(2500))
                .andExpect(jsonPath("$.data.orderItems[0].quantity").value(2));
    }

    @Test
    @DisplayName("주문 내역 조회")
    void getUserOrders() throws Exception {
        // given
        Long userId = 100L;
        OrderResult.OrderHistory orderHistory = OrderResult.OrderHistory.builder()
                .orderId(123L)
                .status("ORDERED")
                .totalAmount(5000L)
                .orderItems(List.of(
                        OrderResult.OrderItemHistory.builder()
                                .optionId(1L)
                                .productName("Product A")
                                .size("L")
                                .color("Red")
                                .quantity(2)
                                .price(2500L)
                                .build()
                ))
                .build();

        List<OrderResult.OrderHistory> orderHistories = List.of(orderHistory);

        when(orderFacade.getUserOrders(userId))
                .thenReturn(orderHistories);

        // then
        mockMvc.perform(get("/api/v1/orders/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data[0].orderId").value(123))
                .andExpect(jsonPath("$.data[0].status").value("ORDERED"))
                .andExpect(jsonPath("$.data[0].totalAmount").value(5000))
                .andExpect(jsonPath("$.data[0].orderItems[0].optionId").value(1))
                .andExpect(jsonPath("$.data[0].orderItems[0].productName").value("Product A"))
                .andExpect(jsonPath("$.data[0].orderItems[0].size").value("L"))
                .andExpect(jsonPath("$.data[0].orderItems[0].color").value("Red"))
                .andExpect(jsonPath("$.data[0].orderItems[0].quantity").value(2))
                .andExpect(jsonPath("$.data[0].orderItems[0].price").value(2500));
    }

    @Test
    @DisplayName("주문 상세 내역 조회")
    void getOrderDetail() throws Exception {
        // given
        Long orderId = 123L;
        OrderResult.FindDetail findDetail = OrderResult.FindDetail.builder()
                .orderId(123L)
                .status("ORDERED")
                .totalAmount(5000L)
                .orderItems(List.of(
                        OrderResult.FindDetail.FindOrderItemDetail.builder()
                                .optionId(1L)
                                .productName("Product A")
                                .size("L")
                                .color("Red")
                                .quantity(2)
                                .price(2500L)
                                .build()
                ))
                .payment(OrderResult.FindDetail.FindPayment.builder()
                        .paymentId(1L)
                        .orderId(123L)
                        .amount(5000L)
                        .paymentDate(LocalDateTime.now())
                        .build())
                .build();

        when(orderFacade.getUserOrderDetail(orderId))
                .thenReturn(findDetail);

        // then
        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.orderId").value(123))
                .andExpect(jsonPath("$.data.status").value("ORDERED"))
                .andExpect(jsonPath("$.data.totalAmount").value(5000))
                .andExpect(jsonPath("$.data.orderItems[0].optionId").value(1))
                .andExpect(jsonPath("$.data.orderItems[0].productName").value("Product A"))
                .andExpect(jsonPath("$.data.orderItems[0].size").value("L"))
                .andExpect(jsonPath("$.data.orderItems[0].color").value("Red"))
                .andExpect(jsonPath("$.data.orderItems[0].quantity").value(2))
                .andExpect(jsonPath("$.data.orderItems[0].price").value(2500))
                .andExpect(jsonPath("$.data.payment.paymentId").value(1))
                .andExpect(jsonPath("$.data.payment.amount").value(5000))
                .andExpect(jsonPath("$.data.payment.paymentDate").exists());
    }
}
