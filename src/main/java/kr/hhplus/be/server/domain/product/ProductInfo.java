package kr.hhplus.be.server.domain.product;

import kr.hhplus.be.server.infrastructure.product.ProductQuery;
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

        public static Detail from(ProductQuery.DetailProjection projection, List<Option> options) {
            return Detail.builder()
                    .productId(projection.getProductId())
                    .name(projection.getName())
                    .price(projection.getPrice())
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

        public static Option from(ProductQuery.OptionProjection projection) {
            return Option.builder()
                    .optionId(projection.getOptionId())
                    .size(projection.getSize())
                    .color(projection.getColor())
                    .stockQuantity(projection.getStockQuantity())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class PriceOption {
        private final long optionId;
        private final long price;
        private final long stockQuantity;

        public static PriceOption from(ProductQuery.PriceOptionProjection projection) {
            return PriceOption.builder()
                    .optionId(projection.getOptionId())
                    .price(projection.getPrice())
                    .stockQuantity(projection.getStockQuantity())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class TopSelling {
        private final long productId;
        private final String name;
        private final long salesCount;

        public static TopSelling from(ProductQuery.TopSellingProjection projection) {
            return TopSelling.builder()
                    .productId(projection.getProductId())
                    .name(projection.getName())
                    .salesCount(projection.getSalesCount())
                    .build();
        }
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
    }

}
