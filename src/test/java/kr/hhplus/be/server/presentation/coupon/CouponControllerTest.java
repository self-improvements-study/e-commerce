package kr.hhplus.be.server.presentation.coupon;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.application.coupon.CouponFacade;
import kr.hhplus.be.server.application.coupon.CouponResult;
import kr.hhplus.be.server.common.exception.BusinessError;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.common.web.ExceptionTranslator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.List;

@WebMvcTest(CouponController.class)
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CouponFacade couponFacade;

    @MockitoBean
    private ExceptionTranslator<BusinessException> exceptionTranslator;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("쿠폰 발급")
    void createUserCoupon() throws Exception {
        // given
        CouponRequest.Issue request = new CouponRequest.Issue(100L, 36L);
        String content = objectMapper.writeValueAsString(request);

        // when
        CouponResult.IssuedCoupon issuedCoupon = CouponResult.IssuedCoupon.builder()
                .userCouponId(23L)
                .couponId(36L)
                .couponName("선착순 쿠폰")
                .discount(3000)
                .startedDate(LocalDateTime.now())
                .endedDate(LocalDateTime.now().plusDays(7))
                .build();

        when(couponFacade.issueCoupon(any()))
                .thenReturn(issuedCoupon);

        // then
        mockMvc.perform(post("/api/v1/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.userCouponId").value(23))
                .andExpect(jsonPath("$.data.couponId").value(36))
                .andExpect(jsonPath("$.data.couponName").value("선착순 쿠폰"))
                .andExpect(jsonPath("$.data.discount").value(3000))
                .andExpect(jsonPath("$.data.startedDate").exists())
                .andExpect(jsonPath("$.data.endedDate").exists());
    }

    @Test
    @DisplayName("유저 쿠폰 조회")
    void findUserCoupons() throws Exception {
        // given
        Long userId = 100L;
        CouponResult.OwnedCoupon ownedCoupon = CouponResult.OwnedCoupon.builder()
                .userCouponId(23L)
                .couponId(36L)
                .couponName("선착순 쿠폰")
                .discount(3000)
                .startedDate(LocalDateTime.now())
                .endedDate(LocalDateTime.now().plusDays(7))
                .build();

        List<CouponResult.OwnedCoupon> ownedCoupons = List.of(ownedCoupon);

        when(couponFacade.getUserCoupons(userId))
                .thenReturn(ownedCoupons);

        // then
        mockMvc.perform(get("/api/v1/coupons/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data[0].userCouponId").value(23))
                .andExpect(jsonPath("$.data[0].couponId").value(36))
                .andExpect(jsonPath("$.data[0].couponName").value("선착순 쿠폰"))
                .andExpect(jsonPath("$.data[0].discount").value(3000))
                .andExpect(jsonPath("$.data[0].startedDate").exists())
                .andExpect(jsonPath("$.data[0].endedDate").exists());
    }
}
