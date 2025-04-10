package kr.hhplus.be.server.domain.product;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.common.entity.AuditableEntity;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "product_option")
public class ProductOption extends AuditableEntity {

    /**
     * 상품 옵션 아이디
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_option_id", nullable = false, updatable = false)
    private Long id;

    /**
     * 상품 아이디
     */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /**
     * 사이즈
     */
    @Column(name = "size", nullable = true)
    private String size;

    /**
     * 색상
     */
    @Column(name = "color", nullable = true)
    private String color;
}
