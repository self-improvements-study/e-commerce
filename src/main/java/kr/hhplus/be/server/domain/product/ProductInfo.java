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
        private long productId;
        private String name;
        private long price;
        private List<Option> options;

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
        private long optionId;
        private String size;
        private String color;
        private long stockQuantity;
    }

    @Getter
    @Builder
    public static class PriceOption {
        private long optionId;
        private long price;
        private long stockQuantity;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    public static class ProductSalesData {
        private List<TopSelling> list;

        public ProductSalesData(List<TopSelling> list) {
            this.list = list;
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    public static class TopSelling {
        private Long productId;
        private String name;
        private Long salesCount;

        public TopSelling(Long productId, String name, Long salesCount) {
            this.productId = productId;
            this.name = name;
            this.salesCount = salesCount;
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

        public static OptionStock from(Stock stock) {
            return OptionStock.builder()
                    .optionId(stock.getProductOptionId())
                    .stockId(stock.getId())
                    .quantity(stock.getQuantity())
                    .build();
        }
    }

}
