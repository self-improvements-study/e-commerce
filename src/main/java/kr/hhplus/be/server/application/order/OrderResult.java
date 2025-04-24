package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderInfo;
import kr.hhplus.be.server.domain.payment.PaymentInfo;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderResult {

    @Getter
    @Builder
    public static class OrderSummary {
        private long orderId;
        private long userId;
        private LocalDateTime orderDate;
        private Order.Status status;
        private long totalPrice;
        private List<OrderItemSummary> orderItems;

        public static OrderSummary from(OrderInfo.OrderSummary order) {
            List<OrderItemSummary> itemResponses = order.getOrderItems().stream()
                    .map(OrderItemSummary::from)
                    .collect(Collectors.toList());

            return OrderSummary.builder()
                    .orderId(order.getOrderId())
                    .userId(order.getUserId())
                    .orderDate(order.getOrderDate())
                    .status(order.getStatus())
                    .totalPrice(order.getTotalPrice())
                    .orderItems(itemResponses)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class OrderItemSummary {
        private long optionId;
        private long originalPrice;
        private int quantity;
        private Long userCouponId;

        public static OrderItemSummary from(OrderInfo.OrderItemSummary orderItem) {
            return OrderItemSummary.builder()
                    .optionId(orderItem.getOptionId())
                    .originalPrice(orderItem.getOriginalPrice())
                    .quantity(orderItem.getQuantity())
                    .userCouponId(orderItem.getUserCouponId())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class OrderHistory {
        private long orderId;
        private String status;
        private long totalAmount;
        private List<OrderItemHistory> orderItems;

        public static OrderHistory from(OrderInfo.OrderHistory orderHistory) {
            List<OrderItemHistory> itemSummaries = orderHistory.getOrderItems().stream()
                    .map(OrderItemHistory::from)
                    .collect(Collectors.toList());

            return OrderHistory.builder()
                    .orderId(orderHistory.getOrderId())
                    .status(orderHistory.getStatus().name())
                    .totalAmount(orderHistory.getTotalAmount())
                    .orderItems(itemSummaries)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class OrderItemHistory {
        private long optionId;
        private String productName;
        private String size;
        private String color;
        private Integer quantity;
        private long price;

        public static OrderItemHistory from(OrderInfo.OrderItemDetail orderItemDetail) {
            return OrderItemHistory.builder()
                    .optionId(orderItemDetail.getOptionId())
                    .productName(orderItemDetail.getProductName())
                    .size(orderItemDetail.getSize())
                    .color(orderItemDetail.getColor())
                    .quantity(orderItemDetail.getQuantity())
                    .price(orderItemDetail.getPrice())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class FindDetail {
        private long orderId;
        private String status;
        private long totalAmount;
        private List<FindOrderItemDetail> orderItems;
        private FindPayment payment;

        public static FindDetail from(OrderInfo.OrderHistory orderHistory, PaymentInfo.PaymentSummary paymentInfo) {
            List<FindOrderItemDetail> items = orderHistory.getOrderItems().stream()
                    .map(FindOrderItemDetail::from)
                    .collect(Collectors.toList());

            return FindDetail.builder()
                    .orderId(orderHistory.getOrderId())
                    .status(orderHistory.getStatus().name())
                    .totalAmount(orderHistory.getTotalAmount())
                    .orderItems(items)
                    .payment(paymentInfo != null ? FindPayment.from(paymentInfo) : null)
                    .build();
        }

        @Getter
        @Builder
        public static class FindOrderItemDetail {
            private long optionId;
            private String productName;
            private String size;
            private String color;
            private int quantity;
            private long price;

            public static FindOrderItemDetail from(OrderInfo.OrderItemDetail orderItemDetail) {
                return FindOrderItemDetail.builder()
                        .optionId(orderItemDetail.getOptionId())
                        .productName(orderItemDetail.getProductName())
                        .size(orderItemDetail.getSize())
                        .color(orderItemDetail.getColor())
                        .quantity(orderItemDetail.getQuantity())
                        .price(orderItemDetail.getPrice())
                        .build();
            }
        }

        @Getter
        @Builder
        public static class FindPayment {
            private long paymentId;
            private long orderId;
            private long amount;
            private LocalDateTime paymentDate;

            public static FindPayment from(PaymentInfo.PaymentSummary paymentSummary) {
                return FindPayment.builder()
                        .paymentId(paymentSummary.getPaymentId())
                        .orderId(paymentSummary.getOrderId())
                        .amount(paymentSummary.getAmount())
                        .paymentDate(paymentSummary.getPaymentDate())
                        .build();
            }
        }
    }


}
