package kr.hhplus.be.server.domain.order;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class OrderQuery {

    public record OrderItemProjection(
            long optionId,
            long productId,
            String productName,
            String size,
            String color,
            int quantity,
            Long userCouponId,
            long price,
            LocalDateTime orderDate
    ) {
        @QueryProjection
        public OrderItemProjection {}

        public OrderInfo.OrderItemDetail to() {
            return OrderInfo.OrderItemDetail.builder()
                    .optionId(optionId)
                    .productId(productId)
                    .productName(productName)
                    .size(size)
                    .color(color)
                    .quantity(quantity)
                    .userCouponId(userCouponId)
                    .price(price)
                    .orderDate(LocalDate.from(orderDate))
                    .build();
        }
    }
}
