package kr.hhplus.be.server.api.point.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.api.point.presentation.controller.dto.request.ChargePointRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PointController.class)
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("포인트 조회")
    void getUserPoint() throws Exception {
        // given
        long userId = 100L;

        mockMvc
                .perform(get("/api/v1/points/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo(200)))
                .andExpect(jsonPath("$.message", equalTo("성공")))
                .andExpect(jsonPath("$.data.balance", equalTo(15000)));
    }

    @Test
    @DisplayName("포인트 충전")
    void chargePoint() throws Exception {
        // given
        ChargePointRequest request = new ChargePointRequest(1L, 20000L);
        String content = objectMapper.writeValueAsString(request);

        mockMvc
                .perform(post("/api/v1/points/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo(200)))
                .andExpect(jsonPath("$.message", equalTo("성공")))
                .andExpect(jsonPath("$.data.balance", equalTo(20000)));
    }

    @Test
    @DisplayName("포인트 내역 조회")
    void findPointHistories() throws Exception {
        // given
        long userId = 100L;

        mockMvc
                .perform(get("/api/v1/points/histories/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo(200)))
                .andExpect(jsonPath("$.message", equalTo("성공")))
                .andExpect(jsonPath("$.data[0].pointHistoryId", equalTo(1)))
                .andExpect(jsonPath("$.data[0].type", equalTo("CHARGE")))
                .andExpect(jsonPath("$.data[0].amount", equalTo(10000)))
                .andExpect(jsonPath("$.data[0].transactionDate", notNullValue()))
                .andExpect(jsonPath("$.data[1].pointHistoryId", equalTo(2)))
                .andExpect(jsonPath("$.data[1].type", equalTo("USE")))
                .andExpect(jsonPath("$.data[1].amount", equalTo(5000)))
                .andExpect(jsonPath("$.data[1].transactionDate", notNullValue()));
    }
}
