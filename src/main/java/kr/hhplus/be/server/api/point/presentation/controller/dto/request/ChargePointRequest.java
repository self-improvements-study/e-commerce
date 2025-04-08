package kr.hhplus.be.server.api.point.presentation.controller.dto.request;

public record ChargePointRequest(
        Long userId,
        Long amount
) {
}
