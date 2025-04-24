package kr.hhplus.be.server.infrastructure.product;

import kr.hhplus.be.server.domain.product.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockJpaRepository extends JpaRepository<Stock, Long> {
    List<Stock> findByProductOptionIdIn(List<Long> optionIds);
}
