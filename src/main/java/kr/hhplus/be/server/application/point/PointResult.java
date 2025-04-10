package kr.hhplus.be.server.application.point;

import kr.hhplus.be.server.domain.point.PointHistory;
import kr.hhplus.be.server.domain.point.PointInfo;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PointResult {

    @Getter
    @Builder
    public static class UserPoint {
        private long userId;
        private long balance;

        public static UserPoint from(PointInfo.Balance info) {
            return UserPoint.builder()
                    .userId(info.getUserId())
                    .balance(info.getBalance())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class Charge {
        private long pointId;
        private long userId;
        private long balance;
        private LocalDateTime createdDate;
        private LocalDateTime lastModifiedDate;

        public static PointResult.Charge from(PointInfo.Increase info) {
            return PointResult.Charge.builder()
                    .pointId(info.getPointId())
                    .userId(info.getUserId())
                    .balance(info.getBalance())
                    .createdDate(info.getCreatedDate())
                    .lastModifiedDate(info.getLastModifiedDate())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class History {
        private long pointHistoryId;
        private long userId;
        private long amount;
        private PointHistory.Type type;
        private LocalDateTime createDate;

        public static PointResult.History from(PointInfo.History info) {
            return PointResult.History.builder()
                    .pointHistoryId(info.getPointId())
                    .userId(info.getUserId())
                    .amount(info.getAmount())
                    .type(info.getType())
                    .createDate(info.getCreatedDate())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class Payment {
        private long userId;
        private long amount;
    }

}
