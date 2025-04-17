package kr.hhplus.be.server.domain.order;

import com.querydsl.core.annotations.QueryProjection;

public class OrderQuery {

    public record OrderItemProjection(
            long optionId,
            String productName,
            String size,
            String color,
            int quantity,
            Long userCouponId,
            long price
    ) {
        @QueryProjection
        public OrderItemProjection {}

        public OrderInfo.OrderItemDetail to() {
            return OrderInfo.OrderItemDetail.builder()
                    .optionId(optionId)
                    .productName(productName)
                    .size(size)
                    .color(color)
                    .quantity(quantity)
                    .userCouponId(userCouponId)
                    .price(price)
                    .build();
        }
    }
}
