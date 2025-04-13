package kr.hhplus.be.server.presentation.point;

import kr.hhplus.be.server.application.point.PointResult;
import kr.hhplus.be.server.domain.point.PointHistory;

import java.time.LocalDateTime;

public record PointResponse() {

    public record Charge(
            long id,
            long userId,
            long balance,
            LocalDateTime createdDate,
            LocalDateTime lastModifiedDate
    ) {
        public static Charge from(PointResult.Charge charge) {
            return new Charge(
                    charge.getPointId(),
                    charge.getUserId(),
                    charge.getBalance(),
                    charge.getCreatedDate(),
                    charge.getLastModifiedDate()
            );
        }
    }

        public record History(
                long pointHistoryId,
                long userId,
                long amount,
                PointHistory.Type type,
                LocalDateTime createDate
        ) {
            public static History from(PointResult.History history) {
                return new History(
                        history.getPointHistoryId(),
                        history.getUserId(),
                        history.getAmount(),
                        history.getType(),
                        history.getCreateDate()
                );
            }
        }

        public record UserPoint(
                long userId,
                long balance

        ) {
            public static UserPoint from(PointResult.UserPoint pointResult) {
                return new UserPoint(
                        pointResult.getUserId(),
                        pointResult.getBalance()
                );
            }
        }
}