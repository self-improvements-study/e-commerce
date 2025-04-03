package kr.hhplus.be.server.api.coupon.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.api.coupon.presentation.controller.dto.request.CreateUserCouponRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CouponController.class)
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("쿠폰 발급")
    void createUserCoupon() throws Exception {
        // given
        CreateUserCouponRequest request = new CreateUserCouponRequest(100L, 36L);
        String content = new ObjectMapper().writeValueAsString(request);

        mockMvc
                // when
                .perform(post("/api/v1/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
                )
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo(200)))
                .andExpect(jsonPath("$.message", equalTo("성공")))
                .andExpect(jsonPath("$.data.userCouponId", equalTo(23)))
                .andExpect(jsonPath("$.data.couponId", equalTo(36)))
                .andExpect(jsonPath("$.data.couponName", equalTo("선착순 쿠폰")))
                .andExpect(jsonPath("$.data.discount", equalTo(3000)))
                .andExpect(jsonPath("$.data.startedDate", notNullValue()))
                .andExpect(jsonPath("$.data.endedDate", notNullValue()));
    }

    @Test
    @DisplayName("유저 쿠폰 조회")
    void findUserCoupons() throws Exception {
        // given
        long userId = 100L;

        mockMvc
                // when
                .perform(get("/api/v1/coupons/{userId}", userId))

                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo(200)))
                .andExpect(jsonPath("$.message", equalTo("성공")))
                .andExpect(jsonPath("$.data[0].userCouponId", equalTo(23)))
                .andExpect(jsonPath("$.data[0].couponId", equalTo(36)))
                .andExpect(jsonPath("$.data[0].couponName", equalTo("선착순 쿠폰")))
                .andExpect(jsonPath("$.data[0].discount", equalTo(3000)))
                .andExpect(jsonPath("$.data[0].startedDate", notNullValue()))
                .andExpect(jsonPath("$.data[0].endedDate", notNullValue()));
    }

}
