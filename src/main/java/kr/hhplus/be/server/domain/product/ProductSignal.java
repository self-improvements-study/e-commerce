package kr.hhplus.be.server.domain.product;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.common.entity.AuditableEntity;
import lombok.*;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "product_signal")
public class ProductSignal extends AuditableEntity {

    /**
     * 아이디
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_signal_id", nullable = false, updatable = false)
    private Long id;

    /**
     * 상품 ID
     */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /**
     * 날짜
     */
    @Column(name = "date", nullable = false)
    private LocalDate date;

    /**
     * 상품명
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * 주문 수량 (누계)
     */
    @Column(name = "order_count", nullable = false)
    private Long orderCount;

    public void incrementOrderCount(long orderCount) {

        this.orderCount += orderCount;
    }

    public void decreaseOrderCount(long orderCount) {

        this.orderCount -= orderCount;
    }

}
