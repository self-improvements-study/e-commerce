package kr.hhplus.be.server.domain.product;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProductCommand {

    @Getter
    @Builder
    public static class IncreaseStock {
        private List<OptionStock> optionStocks;

        public static IncreaseStock of(List<OptionStock> optionStocks) {
            return IncreaseStock.builder()
                    .optionStocks(optionStocks)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class DecreaseStock {
        private List<OptionStock> optionStocks;

        public static DecreaseStock of(List<OptionStock> optionStocks) {
            return DecreaseStock.builder()
                    .optionStocks(optionStocks)
                    .build();
        }

    }

    @Getter
    @Builder
    public static class OptionStock {
        private long optionId;
        private long quantity;

        public static OptionStock of(long optionId, long quantity) {
            return OptionStock.builder()
                    .optionId(optionId)
                    .quantity(quantity)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class OptionIds {
        private List<Long> optionIds;

        public static OptionIds of(List<Long> optionIds) {
            return OptionIds.builder()
                    .optionIds(optionIds)
                    .build();
        }
    }

}
