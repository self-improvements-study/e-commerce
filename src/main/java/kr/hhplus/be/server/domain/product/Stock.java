package kr.hhplus.be.server.domain.product;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.exception.BusinessError;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.common.entity.AuditableEntity;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "stock")
public class Stock extends AuditableEntity {

    /**
     * 재고 아이디
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "product_option_id", nullable = false)
    private Long productOptionId;

    /**
     * 재고
     */
    @Column(name = "quantity", nullable = false)
    private Long quantity;

    public void increase(long quantity) {

        // 증가시킬 수량은 0보다 커야 함
        if (quantity <= 0) {
            throw new BusinessException(BusinessError.STOCK_QUANTITY_INVALID);
        }

        this.quantity += quantity;
    }

    public void decrease(long quantity) {

        // 차감할 수량은 0보다 커야 함
        if (quantity <= 0) {
            throw new BusinessException(BusinessError.STOCK_QUANTITY_INVALID);
        }

        // 현재 재고보다 많은 수량을 차감할 수 없음
        if (this.quantity < quantity) {
            throw new BusinessException(BusinessError.STOCK_QUANTITY_EXCEEDED);
        }

        this.quantity -= quantity;
    }

}
