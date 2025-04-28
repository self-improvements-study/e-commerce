package kr.hhplus.be.server.infrastructure.product;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.domain.product.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StockJpaRepository extends JpaRepository<Stock, Long> {
    List<Stock> findByProductOptionIdIn(List<Long> optionIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Stock s WHERE s.productOptionId IN :optionIds ORDER BY s.productOptionId")
    List<Stock> findByProductOptionIdInWithLock(@Param("optionIds") List<Long> optionIds);
}
