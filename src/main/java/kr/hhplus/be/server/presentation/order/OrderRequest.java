package kr.hhplus.be.server.presentation.order;

import io.micrometer.common.lang.Nullable;
import kr.hhplus.be.server.application.order.OrderCriteria;
import kr.hhplus.be.server.domain.order.OrderCommand;

import java.util.List;
import java.util.stream.Collectors;

public record OrderRequest() {

    public record Create(
            Long userId,
            List<CreateOrderItem> orderItems
    ) {
        public record CreateOrderItem(
                long optionId,
                int quantity,
                @Nullable
                Long userCouponId
        ) {}

        public OrderCriteria.Detail toCriteria() {
            List<OrderCriteria.Item> items = orderItems.stream()
                    .map(item -> OrderCriteria.Item.builder()
                            .optionId(item.optionId())
                            .quantity(item.quantity())
                            .userCouponId(item.userCouponId())
                            .build())
                    .collect(Collectors.toList());

            return OrderCriteria.Detail.builder()
                    .userId(userId)
                    .items(items)
                    .build();
        }

        public OrderCommand.Detail toCommand() {
            List<OrderCommand.Item> items = orderItems.stream()
                    .map(item -> OrderCommand.Item.builder()
                            .optionId(item.optionId())
                            .quantity(item.quantity())
                            .userCouponId(item.userCouponId())
                            .build())
                    .collect(Collectors.toList());

            return OrderCommand.Detail.builder()
                    .userId(userId)
                    .items(items)
                    .build();
        }
    }




}
