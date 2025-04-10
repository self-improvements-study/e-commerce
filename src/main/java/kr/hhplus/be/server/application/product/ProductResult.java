package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.ProductInfo;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProductResult {

    @Getter
    @Builder
    public static class Detail {
        private long productId;
        private String name;
        private long price;
        private List<Option> options;

        public static Detail from(ProductInfo.Detail detail) {
            return Detail.builder()
                    .productId(detail.getProductId())
                    .name(detail.getName())
                    .price(detail.getPrice())
                    .options(detail.getOptions().stream()
                            .map(Option::from)
                            .toList())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class Option {
        private long optionId;
        private String size;
        private String color;
        private long stock;

        public static Option from(ProductInfo.Option option) {
            return Option.builder()
                    .optionId(option.getOptionId())
                    .size(option.getSize())
                    .color(option.getColor())
                    .stock(option.getStockQuantity())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class TopSelling {
        private long productId;
        private String name;
        private long salesCount;

        public static TopSelling from(ProductInfo.TopSelling info) {
            return TopSelling.builder()
                    .productId(info.getProductId())
                    .name(info.getName())
                    .salesCount(info.getSalesCount())
                    .build();
        }
    }
}
