package kr.hhplus.be.server.domain.order;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class OrderEvent {

    @Getter
    public static class Send {

        private Long id;

        private Long userId;

        private LocalDateTime orderDate;

        private Order.Status status;

        private Long totalPrice;

        private List<Item> oderItem;

        public static Send from(Order order, List<OrderInfo.OrderItemDetail> items) {
            Send send = new Send();
            send.id = order.getId();
            send.userId = order.getUserId();
            send.orderDate = order.getOrderDate();
            send.status = order.getStatus();
            send.totalPrice = order.getTotalPrice();
            send.oderItem = items.stream().map(Item::from).toList();

            return send;
        }

        @Getter
        @Builder
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

            public static Send.Item from(OrderInfo.OrderItemDetail detail) {
                return Send.Item.builder()
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
