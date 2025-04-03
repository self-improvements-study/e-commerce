package kr.hhplus.be.server.api.point.presentation.controller;

import kr.hhplus.be.server.api.payment.presentation.controller.PaymentController;
import kr.hhplus.be.server.api.point.presentation.controller.dto.request.ChargePointRequest;
import kr.hhplus.be.server.api.point.presentation.controller.dto.response.ChargePointResponse;
import kr.hhplus.be.server.api.point.presentation.controller.dto.response.FindPointHistoryResponse;
import kr.hhplus.be.server.api.point.presentation.controller.dto.response.FindUserPointResponse;
import kr.hhplus.be.server.common.response.CommonResponse;
import kr.hhplus.be.server.config.swagger.api.PointApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping("/api/v1/points")
public class PointController implements PointApi {

    private static final Logger logger = LoggerFactory.getLogger(PointController.class);

    // 포인트 조회
    @GetMapping("/{userId}")
    public CommonResponse<FindUserPointResponse> getUserPoint(@PathVariable Long userId) {
        FindUserPointResponse data = new FindUserPointResponse(15000L);
        return CommonResponse.success(data);
    }


    // 포인트 충전
    @PostMapping("/charge")
    public CommonResponse<ChargePointResponse> chargePoint(
            @RequestBody ChargePointRequest request
    ) {
        ChargePointResponse data = new ChargePointResponse(20000L);

        logger.info("컨트롤러 진입 - 포인트 충전 요청");

        return CommonResponse.success(data);
    }

    // 포인트 내역 조회
    @GetMapping("/histories/{userId}")
    public CommonResponse<List<FindPointHistoryResponse>> findPointHistories(
            @PathVariable Long userId
    ) {
        List<FindPointHistoryResponse> histories = List.of(
                new FindPointHistoryResponse(
                        1L,
                        "CHARGE",
                        10000L,
                        LocalDateTime.of(2024, 4, 2, 10, 15, 30)
                ),
                new FindPointHistoryResponse(
                        2L, "USE",
                        5000L,
                        LocalDateTime.of(2024, 4, 3, 12, 30, 45)
                )
        );

        return CommonResponse.success(histories);
    }
}

