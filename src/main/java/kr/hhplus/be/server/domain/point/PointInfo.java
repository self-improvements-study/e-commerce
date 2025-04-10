package kr.hhplus.be.server.domain.point;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PointInfo {

    @Getter
    @Builder
    public static class Balance {
        private long pointHistoryId;
        private long userId;
        private long balance;
        private LocalDateTime createdDate;
        private LocalDateTime lastModifiedDate;


        public static Balance from(Point point) {

            return Balance.builder()
                    .pointHistoryId(point.getId())
                    .userId(point.getUserId())
                    .balance(point.getBalance())
                    .createdDate(point.getCreatedDate())
                    .lastModifiedDate(point.getLastModifiedDate())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class Increase {
        private long pointId;
        private long userId;
        private long balance;
        private LocalDateTime createdDate;
        private LocalDateTime lastModifiedDate;

        public static Increase from(Point point) {

            return Increase.builder()
                    .pointId(point.getId())
                    .userId(point.getUserId())
                    .balance(point.getBalance())
                    .createdDate(point.getCreatedDate())
                    .lastModifiedDate(point.getLastModifiedDate())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class Decrease {
        private long pointId;
        private long userId;
        private long balance;
        private LocalDateTime createdDate;
        private LocalDateTime lastModifiedDate;

        public static Decrease from(Point point) {

            return Decrease.builder()
                    .pointId(point.getId())
                    .userId(point.getUserId())
                    .balance(point.getBalance())
                    .createdDate(point.getCreatedDate())
                    .lastModifiedDate(point.getLastModifiedDate())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class History {
        private long pointId;
        private long userId;
        private long amount;
        private PointHistory.Type type;
        private LocalDateTime createdDate;
        private LocalDateTime lastModifiedDate;

        public static History from(PointHistory pointHistory) {

            return History.builder()
                    .pointId(pointHistory.getId())
                    .userId(pointHistory.getUserId())
                    .amount(pointHistory.getAmount())
                    .type(pointHistory.getType())
                    .createdDate(pointHistory.getCreatedDate())
                    .lastModifiedDate(pointHistory.getLastModifiedDate())
                    .build();
        }
    }

}
