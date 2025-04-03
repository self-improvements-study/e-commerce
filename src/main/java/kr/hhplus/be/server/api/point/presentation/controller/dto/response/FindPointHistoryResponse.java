package kr.hhplus.be.server.api.point.presentation.controller.dto.response;

import java.time.LocalDateTime;

public record FindPointHistoryResponse(
        Long pointHistoryId,
        String type,
        Long amount,
        LocalDateTime transactionDate
) {}
