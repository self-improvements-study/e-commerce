package kr.hhplus.be.server.presentation.point;

import kr.hhplus.be.server.application.point.PointFacade;
import kr.hhplus.be.server.application.point.PointResult;
import kr.hhplus.be.server.config.swagger.api.PointApi;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/points")
public class PointController implements PointApi {

    private final PointFacade pointFacade;

    // 포인트 조회
    @GetMapping("/{userId}")
    public PointResponse.UserPoint getUserPoint(@PathVariable Long userId) {

        PointResult.UserPoint pointBalance = pointFacade.getUserPoint(userId);

        return PointResponse.UserPoint.from(pointBalance);
    }


    // 포인트 충전
    @PostMapping("/charge")
    public PointResponse.Charge chargePoint(
            @RequestBody PointRequest.Charge request
    ) {

        PointResult.Charge charge = pointFacade.charge(request.userId(), request.amount());

        return PointResponse.Charge.from(charge);
    }

    // 포인트 내역 조회
    @GetMapping("/histories/{userId}")
    public List<PointResponse.History> findPointHistories(
            @PathVariable Long userId
    ) {
        List<PointResult.History> histories = pointFacade.findPointHistories(userId);

        return histories.stream()
                        .map(PointResponse.History::from)
                        .collect(Collectors.toList());

    }
}

