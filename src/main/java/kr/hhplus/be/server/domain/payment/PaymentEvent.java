package kr.hhplus.be.server.domain.payment;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

public class PaymentEvent {

    @Getter
    @Builder
    public static class CreatePayment {
        private long userId;
        private long orderId;
        private long amount;
        private List<CreatePayment.OptionStock> optionStockList;
        private List<CreatePayment.ProductSignal> productSignalList;
        private List<CreatePayment.UserCoupon> userCouponList;

        public static CreatePayment from(long userId,
                                       long orderId,
                                       long amount,
                                       List<CreatePayment.OptionStock> optionStockList,
                                       List<CreatePayment.ProductSignal> productSignalList,
                                       List<CreatePayment.UserCoupon> userCouponList
        ) {
            return CreatePayment.builder()
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

            public static CreatePayment.OptionStock of(Long optionId, int quantity) {
                return CreatePayment.OptionStock.builder()
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

            public static CreatePayment.ProductSignal of(Long productId, LocalDate date, String name, Integer quantity) {
                return CreatePayment.ProductSignal.builder()
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

            public static CreatePayment.UserCoupon of(Long userCouponId) {
                return CreatePayment.UserCoupon.builder()
                        .userCouponId(userCouponId)
                        .build();
            }
        }
    }

}
