package kr.hhplus.be.server.domain.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class OrderEvent {

    @Getter
    public static class OrderCompleted {

        private Long id;

        private Long userId;

        private LocalDateTime orderDate;

        private Order.Status status;

        private Long totalPrice;

        private List<Item> oderItem;

        public static OrderCompleted from(Order order, List<OrderInfo.OrderItemDetail> items) {
            OrderCompleted orderCompleted = new OrderCompleted();
            orderCompleted.id = order.getId();
            orderCompleted.userId = order.getUserId();
            orderCompleted.orderDate = order.getOrderDate();
            orderCompleted.status = order.getStatus();
            orderCompleted.totalPrice = order.getTotalPrice();
            orderCompleted.oderItem = items.stream().map(Item::from).toList();

            return orderCompleted;
        }

        @Getter
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Item {

            private long optionId;

            private long productId;

            private String productName;

            private String size;

            private String color;

            private int quantity;

            private Long userCouponId;

            private long price;

            private LocalDate orderDate;

            public static Item from(OrderInfo.OrderItemDetail detail) {
                return Item.builder()
                        .optionId(detail.getOptionId())
                        .productId(detail.getProductId())
                        .productName(detail.getProductName())
                        .size(detail.getSize())
                        .color(detail.getColor())
                        .quantity(detail.getQuantity())
                        .userCouponId(detail.getUserCouponId())
                        .price(detail.getPrice())
                        .orderDate(detail.getOrderDate())
                        .build();
            }

        }
    }

    @Getter
    public static class CreateOrder {

        private Long orderId;

        private Long userId;

        private LocalDateTime orderDate;

        private Order.Status status;

        private Long totalPrice;

        private List<OrderEvent.CreateOrder.Item> orderItem;

        public static OrderEvent.CreateOrder from(Order order, List<OrderCommand.Item> items) {
            OrderEvent.CreateOrder success = new OrderEvent.CreateOrder();
            success.orderId = order.getId();
            success.userId = order.getUserId();
            success.orderDate = order.getOrderDate();
            success.status = order.getStatus();
            success.totalPrice = order.getTotalPrice();
            success.orderItem = items.stream().map(OrderEvent.CreateOrder.Item::from).toList();

            return success;
        }

        @Getter
        @Builder
        public static class Item {

            private long optionId;

            private int quantity;

            private Long userCouponId;

            private long price;

            public static OrderEvent.CreateOrder.Item from(OrderCommand.Item detail) {
                return OrderEvent.CreateOrder.Item.builder()
                        .optionId(detail.getOptionId())
                        .quantity(detail.getQuantity())
                        .userCouponId(detail.getUserCouponId())
                        .price(detail.getPrice())
                        .build();
            }

        }
    }

}
