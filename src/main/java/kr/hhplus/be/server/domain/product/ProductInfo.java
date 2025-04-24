package kr.hhplus.be.server.domain.product;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProductInfo {

    @Getter
    @Builder
    public static class Detail {
        private final long productId;
        private final String name;
        private final long price;
        private final List<Option> options;

        public static Detail from(Product product, List<Option> options) {
            return Detail.builder()
                    .productId(product.getId())
                    .name(product.getName())
                    .price(product.getPrice())
                    .options(options)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class Option {
        private final long optionId;
        private final String size;
        private final String color;
        private final long stockQuantity;
    }

    @Getter
    @Builder
    public static class PriceOption {
        private final long optionId;
        private final long price;
        private final long stockQuantity;
    }

    @Getter
    @Builder
    public static class TopSelling {
        private final long productId;
        private final String name;
        private final long salesCount;
    }

    @Getter
    @Builder
    public static class IncreaseStock {
        private List<OptionStock> optionStocks;
    }

    @Getter
    @Builder
    public static class DecreaseStock {
        private List<OptionStock> optionStocks;
    }

    @Getter
    @Builder
    public static class OptionStock {
        private long optionId;
        private long stockId;
        private long quantity;

        public static OptionStock from(Stock stock) {
            return OptionStock.builder()
                    .optionId(stock.getProductOptionId())
                    .stockId(stock.getId())
                    .quantity(stock.getQuantity())
                    .build();
        }
    }

}
