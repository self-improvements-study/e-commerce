package kr.hhplus.be.server.presentation.product;

import kr.hhplus.be.server.application.point.PointResult;
import kr.hhplus.be.server.application.product.ProductResult;
import kr.hhplus.be.server.domain.point.PointHistory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record ProductResponse() {

    public record Detail(
            long productId,
            String name,
            long price,
            List<Option> options
    ) {
        public static Detail from(ProductResult.Detail detail) {
            return new Detail(
                    detail.getProductId(),
                    detail.getName(),
                    detail.getPrice(),
                    detail.getOptions().stream()
                            .map(Option::from)
                            .toList()
            );
        }
    }

    public record Option(
            long optionId,
            String size,
            String color,
            long stock
    ) {
        public static Option from(ProductResult.Option option) {
            return new Option(
                    option.getOptionId(),
                    option.getSize(),
                    option.getColor(),
                    option.getStock()
            );
        }
    }

    public record TopSelling(
            long productId,
            String name,
            long salesCount
    ) {
        public static TopSelling from(ProductResult.TopSelling result) {
            return new TopSelling(result.getProductId(), result.getName(), result.getSalesCount());
        }

        public static List<TopSelling> from(List<ProductResult.TopSelling> results) {
            return results.stream()
                    .map(TopSelling::from)
                    .collect(Collectors.toList());
        }
    }
}