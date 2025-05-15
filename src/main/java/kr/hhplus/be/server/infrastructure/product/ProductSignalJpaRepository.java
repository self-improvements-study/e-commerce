package kr.hhplus.be.server.infrastructure.product;

import kr.hhplus.be.server.domain.product.ProductSignal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProductSignalJpaRepository extends JpaRepository<ProductSignal, Long> {

    Optional<ProductSignal> findByDateAndProductId(LocalDate date, Long productId);

    @Query("SELECT ps FROM ProductSignal ps " +
            "WHERE ps.date >= :daysAgo " +
            "ORDER BY ps.orderCount DESC")
    List<ProductSignal> findTopOrderedProducts(
            @Param("daysAgo") LocalDate daysAgo,
            @Param("limit") long limit);
}
