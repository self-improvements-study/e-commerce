package kr.hhplus.be.server.application.order;

import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderCriteria {

    @Getter
    @Builder
    public static class Detail {
        private long userId;
        private List<Item> items;

        public List<Long> toOptionIds() {
            return this.items.stream().map(Item::getOptionId).sorted().toList();
        }
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
