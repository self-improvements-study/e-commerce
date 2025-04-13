package kr.hhplus.be.server.domain.point;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PointRepository {

    Optional<Point> findPointByUserId(Long userId);

    List<PointHistory> findPointHistoryByUserId(Long userId);

    Point save(Point point);

    PointHistory save(PointHistory pointHistory);

}
