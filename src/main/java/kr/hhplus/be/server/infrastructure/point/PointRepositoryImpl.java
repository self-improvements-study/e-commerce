package kr.hhplus.be.server.infrastructure.point;

import kr.hhplus.be.server.domain.point.Point;
import kr.hhplus.be.server.domain.point.PointHistory;
import kr.hhplus.be.server.domain.point.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class PointRepositoryImpl implements PointRepository {

    private final PointJpaRepository pointJpaRepository;

    private final PointHistoryJpaRepository pointHistoryJpaRepository;

    @Override
    public Optional<Point> findPointByUserId(Long userId) {
        return pointJpaRepository.findPointByUserId(userId);
    }

    @Override
    public List<PointHistory> findPointHistoryByUserId(Long userId) {
        return pointHistoryJpaRepository.findPointHistoryByUserId(userId);
    }

    @Override
    public Point save(Point point) {
        return pointJpaRepository.save(point);
    }

    @Override
    public PointHistory save(PointHistory pointHistory) {
        return pointHistoryJpaRepository.save(pointHistory);
    }
}
