package kr.hhplus.be.server.application.point;

import kr.hhplus.be.server.domain.point.PointInfo;
import kr.hhplus.be.server.domain.point.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class PointFacade {

    private final PointService pointService;

    @Transactional(readOnly = true)
    public PointResult.UserPoint getUserPoint(long userId) {

        PointInfo.Balance pointBalance = pointService.getBalance(userId);

        return PointResult.UserPoint.from(pointBalance);
    }

    @Transactional
    public PointResult.Charge charge(long userId, long amount) {

        PointInfo.Increase pointBalance = pointService.increase(userId, amount);

        return PointResult.Charge.from(pointBalance);
    }

    @Transactional(readOnly = true)
    public List<PointResult.History> findPointHistories(long userId) {

        List<PointInfo.History> pointHistories = pointService.findPointHistories(userId);

        return pointHistories.stream()
                             .map(PointResult.History::from)
                             .toList();
    }


}
