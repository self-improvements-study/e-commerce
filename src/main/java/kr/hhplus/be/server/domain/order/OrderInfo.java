package kr.hhplus.be.server.domain.order;

import kr.hhplus.be.server.infrastructure.order.OrderQuery;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderInfo {

    @Getter
    @Builder
    public static class OrderHistory {
        private final long orderId;
        private final Order.Status status;
        private final long totalAmount;
        private final List<OrderItemDetail> orderItems;

        public static OrderHistory from(OrderQuery.OrderProjection orderProjection
                , List<OrderItemDetail> orderItems) {
            return OrderHistory.builder()
                    .orderId(orderProjection.getOrderId())
                    .status(Order.Status.valueOf(orderProjection.getStatus()))
                    .totalAmount(orderProjection.getTotalAmount())
                    .orderItems(orderItems)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class OrderItemDetail {
        private final long optionId;
        private final String productName;
        private final String size;
        private final String color;
        private final Integer quantity;
        private final long userCouponId;
        private final long price;

        public static OrderItemDetail from(OrderQuery.OrderItemProjection itemProjection) {
            return OrderItemDetail.builder()
                    .optionId(itemProjection.getOptionId())
                    .productName(itemProjection.getProductName())
                    .size(itemProjection.getSize())
                    .color(itemProjection.getColor())
                    .quantity(itemProjection.getQuantity())
                    .userCouponId(itemProjection.getUserCouponId())
                    .price(itemProjection.getPrice())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class OrderSummary {
        private long orderId;
        private long userId;
        private LocalDateTime orderDate;
        private Order.Status status;
        private long totalPrice;
        private List<OrderItemSummary> orderItems;

        public static OrderSummary from(Order order, List<OrderItem> orderItems) {
            List<OrderItemSummary> itemResponses = orderItems.stream()
                    .map(OrderItemSummary::from)
                    .collect(Collectors.toList());

            return OrderSummary.builder()
                    .orderId(order.getId())
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
        private Integer quantity;
        private Long userCouponId;

        public static OrderItemSummary from(OrderItem orderItem) {
            return OrderItemSummary.builder()
                    .optionId(orderItem.getOptionId())
                    .originalPrice(orderItem.getOriginalPrice())
                    .quantity(orderItem.getQuantity())
                    .userCouponId(orderItem.getUserCouponId())
                    .build();
        }
    }

}
