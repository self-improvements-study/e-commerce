package kr.hhplus.be.server.domain.payment;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentCommand {

    @Getter
    @Builder
    public static class Payment {
        private long userId;
        private long orderId;
        private long amount;
        private List<OptionStock> optionStockList;
        private List<ProductSignal> productSignalList;
        private List<UserCoupon> userCouponList;

        public static Payment of(long userId,
                                 long orderId,
                                 long amount,
                                 List<OptionStock> optionStockList,
                                 List<ProductSignal> productSignalList,
                                 List<UserCoupon> userCouponList
        ) {
            return Payment.builder()
                    .userId(userId)
                    .orderId(orderId)
                    .amount(amount)
                    .optionStockList(optionStockList)
                    .productSignalList(productSignalList)
                    .userCouponList(userCouponList)
                    .build();
        }

        @Getter
        @Builder
        public static class OptionStock {

            private Long optionId;
            private int quantity;

            public static OptionStock of(Long optionId, int quantity) {
                return OptionStock.builder()
                        .optionId(optionId)
                        .quantity(quantity)
                        .build();
            }

        }

        @Getter
        @Builder
        public static class ProductSignal {
            private Long productId;
            private LocalDate date;
            private String name;
            private int quantity;

            public static ProductSignal of(Long productId, LocalDate date, String name, Integer quantity) {
                return ProductSignal.builder()
                        .productId(productId)
                        .date(date)
                        .name(name)
                        .quantity(quantity)
                        .build();
            }
        }

        @Getter
        @Builder
        public static class UserCoupon {
            private Long userCouponId;

            public static UserCoupon of(Long userCouponId) {
                return UserCoupon.builder()
                        .userCouponId(userCouponId)
                        .build();
            }
        }
    }

}
