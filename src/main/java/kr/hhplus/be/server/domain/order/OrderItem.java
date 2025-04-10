package kr.hhplus.be.server.domain.order;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.common.entity.AuditableEntity;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "order_item")
public class OrderItem extends AuditableEntity {

    /**
     * 주문 상품 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id", nullable = false, updatable = false)
    private Long id;

    /**
     * 주문 ID
     */
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    /**
     * 상품 옵션 ID
     */
    @Column(name = "option_id", nullable = false)
    private Long optionId;

    /**
     * 주문 당시 원가격
     */
    @Column(name = "original_price", nullable = false)
    private Long originalPrice;

    /**
     * 수량
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * 유저 쿠폰 ID
     */
    @Column(name = "user_coupon_id")
    private Long userCouponId;

}
