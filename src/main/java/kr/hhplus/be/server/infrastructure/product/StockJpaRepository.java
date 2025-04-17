package kr.hhplus.be.server.infrastructure.product;

import kr.hhplus.be.server.domain.product.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockJpaRepository extends JpaRepository<Stock, Long> {
    List<Stock> findByProductOptionIdIn(List<Long> optionIds);
}
