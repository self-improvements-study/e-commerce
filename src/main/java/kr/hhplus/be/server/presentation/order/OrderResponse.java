package kr.hhplus.be.server.presentation.order;

import kr.hhplus.be.server.application.order.OrderResult;
import kr.hhplus.be.server.domain.order.Order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record OrderResponse() {

    public record Create(
            Long orderId,
            Long userId,
            LocalDateTime orderDate,
            Order.Status status,
            Long totalPrice,
            List<OrderItemSummary> orderItems
    ) {
        public static Create from(OrderResult.OrderSummary orderSummary) {
            List<OrderItemSummary> itemResponses = orderSummary.getOrderItems().stream()
                    .map(OrderItemSummary::from)
                    .collect(Collectors.toList());

            return new Create(
                    orderSummary.getOrderId(),
                    orderSummary.getUserId(),
                    orderSummary.getOrderDate(),
                    orderSummary.getStatus(),
                    orderSummary.getTotalPrice(),
                    itemResponses
            );
        }
    }

    public record OrderItemSummary(
            Long optionId,
            Long originalPrice,
            Integer quantity,
            Long userCouponId
    ) {
        public static OrderItemSummary from(OrderResult.OrderItemSummary orderItemSummary) {
            return new OrderItemSummary(
                    orderItemSummary.getOptionId(),
                    orderItemSummary.getOriginalPrice(),
                    orderItemSummary.getQuantity(),
                    orderItemSummary.getUserCouponId()
            );
        }
    }

    public record Find(
            long orderId,
            String status,
            long totalAmount,
            List<FindOrderItem> orderItems
    ) {


        public static Find from(OrderResult.OrderHistory orderHistory) {
            List<FindOrderItem> orderItemList = orderHistory.getOrderItems().stream()
                    .map(FindOrderItem::from)
                    .collect(Collectors.toList());

            return new Find(
                    orderHistory.getOrderId(),
                    orderHistory.getStatus(),
                    orderHistory.getTotalAmount(),
                    orderItemList
            );
        }

        public record FindOrderItem(
                long optionId,
                String productName,
                String size,
                String color,
                long quantity,
                long price
        ) {
            public static FindOrderItem from(OrderResult.OrderItemHistory orderItemHistory) {
                return new FindOrderItem(
                        orderItemHistory.getOptionId(),
                        orderItemHistory.getProductName(),
                        orderItemHistory.getSize(),
                        orderItemHistory.getColor(),
                        orderItemHistory.getQuantity(),
                        orderItemHistory.getPrice()
                );
            }
        }
    }

        public record FindDetail(
                long orderId,
                String status,
                long totalAmount,
                List<FindOrderItemDetail> orderItems,
                FindPayment payment
        ) {

            public static FindDetail from(OrderResult.FindDetail result) {
                List<FindOrderItemDetail> orderItemDetails = result.getOrderItems().stream()
                        .map(FindOrderItemDetail::from)
                        .toList();

                return new FindDetail(
                        result.getOrderId(),
                        result.getStatus(),
                        result.getTotalAmount(),
                        orderItemDetails,
                        FindPayment.from(result.getPayment())
                );
            }

            public record FindOrderItemDetail(
                    long optionId,
                    String productName,
                    String size,
                    String color,
                    long quantity,
                    long price
            ) {

                public static FindOrderItemDetail from(OrderResult.FindDetail.FindOrderItemDetail detail) {
                    return new FindOrderItemDetail(
                            detail.getOptionId(),
                            detail.getProductName(),
                            detail.getSize(),
                            detail.getColor(),
                            detail.getQuantity(),
                            detail.getPrice()
                    );
                }
            }

                public record FindPayment(
                        long paymentId,
                        long orderId,
                        long amount,
                        LocalDateTime paymentDate
                ) {

                    public static FindPayment from(OrderResult.FindDetail.FindPayment payment) {
                        return new FindPayment(
                                payment.getPaymentId(),
                                payment.getOrderId(),
                                payment.getAmount(),
                                payment.getPaymentDate()
                        );
                    }
                }
        }
    }