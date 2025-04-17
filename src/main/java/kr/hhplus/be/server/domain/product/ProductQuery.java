package kr.hhplus.be.server.domain.product;

import com.querydsl.core.annotations.QueryProjection;

public interface ProductQuery {

    record Option(
            long optionId,
            String size,
            String color,
            long stockQuantity
    ) {
        @QueryProjection
        public Option {}

        public ProductInfo.Option to() {
            return ProductInfo.Option.builder()
                    .optionId(optionId)
                    .size(size)
                    .color(color)
                    .stockQuantity(stockQuantity)
                    .build();
        }
    }

    record PriceOption(
            long optionId,
            long price,
            long stockQuantity
    ) {
        @QueryProjection
        public PriceOption {}

        public ProductInfo.PriceOption to() {
            return ProductInfo.PriceOption.builder()
                    .optionId(optionId)
                    .price(price)
                    .stockQuantity(stockQuantity)
                    .build();
        }
    }

    record TopSelling(
            long productId,
            String name,
            long salesCount
    ) {
        @QueryProjection
        public TopSelling {}

        public ProductInfo.TopSelling to() {
            return ProductInfo.TopSelling.builder()
                    .productId(productId)
                    .name(name)
                    .salesCount(salesCount)
                    .build();
        }
    }
}
