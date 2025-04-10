package kr.hhplus.be.server.domain.point;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PointCommand {

    @Getter
    @Builder
    public static class Search {
        private long userId;

    }

    @Getter
    @Builder
    public static class Charge {
        private long userId;
        private long amount;
    }

    @Getter
    @Builder
    public static class Payment {
        private long userId;
        private long amount;
    }

}
