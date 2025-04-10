package kr.hhplus.be.server.domain.order;

import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderCommand {

    @Getter
    @Builder
    public static class Detail {
        private long userId;
        private List<Item> items;

        public static Detail of(long userId, List<Item> items) {
            return Detail.builder()
                    .userId(userId)
                    .items(items)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class Item {
        private long optionId;
        private long price;
        private int quantity;
        @Nullable
        private Long userCouponId;
        @Nullable
        private Long discount;

        public static Item of(long optionId, long price, int quantity, Long userCouponId, Long discount) {
            return Item.builder()
                    .optionId(optionId)
                    .price(price)
                    .quantity(quantity)
                    .userCouponId(userCouponId)
                    .discount(discount)
                    .build();
        }
    }

}
