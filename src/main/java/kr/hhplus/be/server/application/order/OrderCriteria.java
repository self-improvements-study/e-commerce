package kr.hhplus.be.server.application.order;

import jakarta.annotation.Nullable;
import kr.hhplus.be.server.presentation.order.OrderRequest;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderCriteria {

    @Getter
    @Builder
    public static class Detail {
        private long userId;
        private List<Item> items;
    }

    @Getter
    @Builder
    public static class Item {
        private long optionId;
        private int quantity;
        @Nullable
        private Long userCouponId;
    }

}
