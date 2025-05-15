package kr.hhplus.be.server.domain.product;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

import static java.util.Comparator.comparing;

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
                    .optionStocks(optionStocks.stream()
                            .sorted(comparing(OptionStock::getOptionId))
                            .toList())
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
    public static class ProductSignal {
        private Long productId;
        private LocalDate date;
        private String name;
        private Long quantity;

        public static ProductSignal of(Long productId, LocalDate date, String name, Integer quantity) {
            return ProductSignal.builder()
                    .productId(productId)
                    .date(date)
                    .name(name)
                    .quantity(Long.valueOf(quantity))
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
