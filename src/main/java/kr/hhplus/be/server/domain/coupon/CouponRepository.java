package kr.hhplus.be.server.domain.coupon;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CouponRepository {

    List<UserCoupon> findUserCouponsById(List<Long> userCouponIds);

    List<UserCoupon> saveUserCoupons(List<UserCoupon> userCoupons);

    Optional<Coupon> findCouponById(Long couponId);

    Optional<Coupon> findCouponByIdForUpdate(Long id);

    Coupon save(Coupon coupon);

    UserCoupon saveUserCoupon(UserCoupon userCoupon);

    boolean existsUserCoupon(Long userId, Long couponId);

    List<CouponQuery.OwnedCoupon> findAllOwnedCouponsByUserId(long userId);

    List<CouponQuery.OwnedCoupon> findUserCouponsByIds(List<Long> userCouponIds);

    List<UserCoupon> findUserCouponsByExpiredDate(LocalDateTime expiredDate);

    List<Coupon> findByCouponStatus(Coupon.Status status);

    Optional<Coupon> findByIdForUpdate(Long couponId, Coupon.Status status);

    long countUserCouponByCouponId(Long couponId);

    void saveAll(List<UserCoupon> userCoupons);

    List<UserCoupon> findUserCouponsByCouponId(Long couponId);
}
