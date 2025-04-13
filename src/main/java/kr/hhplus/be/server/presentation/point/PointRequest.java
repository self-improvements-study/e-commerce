package kr.hhplus.be.server.presentation.point;

public record PointRequest() {

    public record Charge(
            long userId,
            long amount
    ) {
    }

}
