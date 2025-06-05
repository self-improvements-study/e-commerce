package kr.hhplus.be.server.infrastructure.coupon;

import kr.hhplus.be.server.domain.coupon.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

public interface UserCouponJpaRepository extends JpaRepository<UserCoupon, Long> {
    boolean existsByUserIdAndCouponId(Long userId, Long couponId);

    @Query("""
            SELECT uc
              FROM UserCoupon uc
              JOIN Coupon c
                ON uc.couponId = c.id
             WHERE c.endedDate < :standardDateTime
               AND uc.used = false
            """)
    List<UserCoupon> findExpiredUserCoupons(@Param("standardDateTime") LocalDateTime standardDateTime);

    long countByCouponId(Long couponId);

    List<UserCoupon> findAllByCouponId(Long couponId);
}
