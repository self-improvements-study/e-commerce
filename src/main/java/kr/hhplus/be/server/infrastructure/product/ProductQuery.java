package kr.hhplus.be.server.infrastructure.product;

public class ProductQuery {

    public interface DetailProjection {
        long getProductId();
        String getName();
        long getPrice();
    }

    public interface OptionProjection {
        long getOptionId();
        String getSize();
        String getColor();
        long getStockQuantity();
    }

    public interface PriceOptionProjection {
        long getOptionId();
        long getPrice();
        long getStockQuantity();
    }

    public interface TopSellingProjection {
        long getProductId();
        String getName();
        long getSalesCount();
    }
}
