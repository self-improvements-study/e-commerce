package kr.hhplus.be.server.domain.coupon;

import kr.hhplus.be.server.infrastructure.coupon.CouponQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository {

    List<CouponQuery.DetailProjection> findUserCouponDetailsById(List<Long> userCouponIds);

    List<UserCoupon> findUserCouponsById(List<Long> userCouponIds);

    List<UserCoupon> saveUserCoupons(List<UserCoupon> userCoupons);

    List<UserCoupon> findAllUserCouponsByUserId(Long userId);

    List<Coupon> findAllByIdIn(List<Long> ids);

    Optional<Coupon> findCouponById(Long couponId);

    UserCoupon saveUserCoupon(UserCoupon userCoupon);

    boolean existsUserCoupon(Long userId, Long couponId);

    List<CouponQuery.OwnedCouponProjection> findAllOwnedCouponsByUserId(long userId);

    List<CouponQuery.OwnedCouponProjection> findUserCouponsByIds(List<Long> userCouponIds);
}
