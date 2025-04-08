package kr.hhplus.be.server.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.api.coupon.presentation.controller.dto.request.CreateUserCouponRequest;
import kr.hhplus.be.server.api.order.presentation.controller.dto.request.CreateOrderRequest;
import kr.hhplus.be.server.api.payment.presentation.controller.dto.request.CreatePaymentRequest;
import kr.hhplus.be.server.api.point.presentation.controller.dto.request.ChargePointRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderPaymentE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private long userId = 100L;
    private long productId = 101L;
    private long couponId = 36L;
    private long orderId;

    @Test
    @DisplayName("포인트 충전 → 쿠폰 발급 → 주문 → 결제 E2E 테스트")
    void endToEndFlow() throws Exception {
        // 1. 포인트 충전
        ChargePointRequest chargeRequest = new ChargePointRequest(userId, 20000L);
        JsonNode chargeJson = postRequest("/api/v1/points/charge", chargeRequest);
        assertThat(chargeJson.path("code").asInt()).isEqualTo(200);

        // 2. 쿠폰 발급
        CreateUserCouponRequest couponRequest = new CreateUserCouponRequest(userId, couponId);
        JsonNode couponJson = postRequest("/api/v1/coupons", couponRequest);
        assertThat(couponJson.path("code").asInt()).isEqualTo(200);
        long userCouponId = couponJson.path("data").path("userCouponId").asLong();

        // 3. 주문 생성
        CreateOrderRequest orderRequest = new CreateOrderRequest(List.of(
                new CreateOrderRequest.CreateOrderItemRequest(productId, 2L, userCouponId)
        ));
        JsonNode orderJson = postRequest("/api/v1/orders/" + userId, orderRequest);
        assertThat(orderJson.path("code").asInt()).isEqualTo(200);
        orderId = orderJson.path("data").path("orderId").asLong();

        // 4. 결제 진행
        CreatePaymentRequest paymentRequest = new CreatePaymentRequest(orderId);
        JsonNode paymentJson = postRequest("/api/v1/payments", paymentRequest);
        assertThat(paymentJson.path("code").asInt()).isEqualTo(200);
    }

    private JsonNode postRequest(String url, Object request) throws Exception {
        ResponseEntity<String> response = restTemplate.postForEntity(url, createHttpEntity(request), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return objectMapper.readTree(response.getBody());
    }

    private HttpEntity<Object> createHttpEntity(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}
