package kr.hhplus.be.server.presentation.point;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.application.point.PointFacade;
import kr.hhplus.be.server.application.point.PointResult;
import kr.hhplus.be.server.domain.point.PointHistory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.when;
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

    @MockitoBean
    private PointFacade pointFacade;

    @Test
    @DisplayName("사용자 포인트 조회")
    void getUserPoint() throws Exception {
        // given
        long userId = 100L;
        PointResult.UserPoint userPoint = PointResult.UserPoint.builder()
                .userId(userId)
                .balance(1000L)
                .build();

        when(pointFacade.getUserPoint(userId)).thenReturn(userPoint);

        // when & then
        mockMvc.perform(get("/api/v1/points/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.balance").value(1000));
    }

    @Test
    @DisplayName("포인트 충전")
    void chargePoint() throws Exception {
        // given
        PointRequest.Charge request = new PointRequest.Charge(100L, 500L);
        String content = new ObjectMapper().writeValueAsString(request);

        PointResult.Charge charge = PointResult.Charge.builder()
                .pointId(1L)
                .userId(100L)
                .balance(1500L)
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .build();

        when(pointFacade.charge(request.userId(), request.amount())).thenReturn(charge);

        // when & then
        mockMvc.perform(post("/api/v1/points/charge")
                        .contentType("application/json")
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userId").value(100))
                .andExpect(jsonPath("$.data.balance").value(1500))
                .andExpect(jsonPath("$.data.createdDate").exists())
                .andExpect(jsonPath("$.data.lastModifiedDate").exists());
    }

    @Test
    @DisplayName("포인트 내역 조회")
    void findPointHistories() throws Exception {
        // given
        long userId = 100L;
        List<PointResult.History> histories = List.of(
                PointResult.History.builder()
                        .pointHistoryId(1L)
                        .userId(userId)
                        .amount(500L)
                        .type(PointHistory.Type.CHARGE)
                        .createDate(LocalDateTime.now())
                        .build(),
                PointResult.History.builder()
                        .pointHistoryId(2L)
                        .userId(userId)
                        .amount(-200L)
                        .type(PointHistory.Type.PAYMENT)
                        .createDate(LocalDateTime.now())
                        .build()
        );

        when(pointFacade.findPointHistories(userId)).thenReturn(histories);

        // when & then
        mockMvc.perform(get("/api/v1/points/histories/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data[0].pointHistoryId").value(1))
                .andExpect(jsonPath("$.data[0].userId").value(userId))
                .andExpect(jsonPath("$.data[0].amount").value(500))
                .andExpect(jsonPath("$.data[0].type").value("CHARGE"))
                .andExpect(jsonPath("$.data[1].pointHistoryId").value(2))
                .andExpect(jsonPath("$.data[1].amount").value(-200))
                .andExpect(jsonPath("$.data[1].type").value("PAYMENT"));
    }

}