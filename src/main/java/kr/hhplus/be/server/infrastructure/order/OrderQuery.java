package kr.hhplus.be.server.infrastructure.order;

public class OrderQuery {

    public interface OrderProjection {
        long getOrderId();
        String getStatus();
        long getTotalAmount();
    }

    public interface OrderItemProjection {
        long getOptionId();
        String getProductName();
        String getSize();
        String getColor();
        int getQuantity();
        long getUserCouponId();
        long getPrice();
    }
}
