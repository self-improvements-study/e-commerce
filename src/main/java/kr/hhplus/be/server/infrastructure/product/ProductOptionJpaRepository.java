package kr.hhplus.be.server.infrastructure.product;

import kr.hhplus.be.server.domain.product.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductOptionJpaRepository extends JpaRepository<ProductOption, Long> {
}
