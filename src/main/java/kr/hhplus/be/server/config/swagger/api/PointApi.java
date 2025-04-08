package kr.hhplus.be.server.config.swagger.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.api.point.presentation.controller.dto.request.ChargePointRequest;
import kr.hhplus.be.server.api.point.presentation.controller.dto.response.ChargePointResponse;
import kr.hhplus.be.server.api.point.presentation.controller.dto.response.FindPointHistoryResponse;
import kr.hhplus.be.server.api.point.presentation.controller.dto.response.FindUserPointResponse;
import kr.hhplus.be.server.common.response.CommonResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Point", description = "포인트 관련 API")
public interface PointApi {

    @Operation(summary = "포인트 조회", description = "사용자의 현재 포인트를 조회합니다.")
    CommonResponse<FindUserPointResponse> getUserPoint(
            @Parameter(description = "유저 ID", example = "1")@PathVariable Long userId);

    @Operation(summary = "포인트 충전", description = "포인트를 충전합니다.")
    CommonResponse<ChargePointResponse> chargePoint(@RequestBody ChargePointRequest request);

    @Operation(summary = "포인트 내역 조회", description = "사용자의 포인트 사용 및 충전 내역을 조회합니다.")
    CommonResponse<List<FindPointHistoryResponse>> findPointHistories(
            @Parameter(description = "유저 ID", example = "1")@PathVariable Long userId);
}