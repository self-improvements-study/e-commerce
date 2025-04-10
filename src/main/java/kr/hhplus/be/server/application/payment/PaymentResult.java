package kr.hhplus.be.server.application.payment;

import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentInfo;
import kr.hhplus.be.server.domain.point.PointHistory;
import kr.hhplus.be.server.domain.point.PointInfo;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PaymentResult {

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

        public static PaymentResult.Charge from(PointInfo.Increase info) {
            return PaymentResult.Charge.builder()
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

        public static PaymentResult.History from(PointInfo.History info) {
            return PaymentResult.History.builder()
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
    public static class PaymentSummary {
        private long paymentId;
        private long orderId;
        private long amount;
        private LocalDateTime paymentDate;

        public static PaymentResult.PaymentSummary from(PaymentInfo.PaymentSummary info) {
            return PaymentResult.PaymentSummary.builder()
                    .paymentId(info.getPaymentId())
                    .orderId(info.getOrderId())
                    .amount(info.getAmount())
                    .paymentDate(info.getPaymentDate())
                    .build();
        }
    }

}
