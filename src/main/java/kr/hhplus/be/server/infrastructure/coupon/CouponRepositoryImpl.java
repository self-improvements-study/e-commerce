package kr.hhplus.be.server.infrastructure.coupon;

import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.hhplus.be.server.domain.coupon.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static kr.hhplus.be.server.domain.coupon.QCoupon.coupon;
import static kr.hhplus.be.server.domain.coupon.QUserCoupon.userCoupon;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepository {

    private final CouponJpaRepository couponJpaRepository;
    private final UserCouponJpaRepository userCouponJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public List<UserCoupon> findUserCouponsById(List<Long> userCouponIds) {
        return userCouponJpaRepository.findAllById(userCouponIds);
    }

    @Override
    public List<UserCoupon> saveUserCoupons(List<UserCoupon> userCoupons) {
        return userCouponJpaRepository.saveAll(userCoupons);
    }

    @Override
    public Optional<Coupon> findCouponById(Long couponId) {
        return couponJpaRepository.findById(couponId);
    }

    @Override
    public Optional<Coupon> findCouponByIdForUpdate(Long id) {
        return couponJpaRepository.findCouponByUserIdForUpdate(id);
    }

    @Override
    public Coupon save(Coupon coupon) {
        return couponJpaRepository.save(coupon);
    }

    @Override
    public UserCoupon saveUserCoupon(UserCoupon userCoupon) {
        return userCouponJpaRepository.save(userCoupon);
    }

    @Override
    public boolean existsUserCoupon(Long userId, Long couponId) {
        return userCouponJpaRepository.existsByUserIdAndCouponId(userId, couponId);
    }

    @Override
    public List<CouponQuery.OwnedCoupon> findAllOwnedCouponsByUserId(long userId) {
        return queryFactory
                .select(new QCouponQuery_OwnedCoupon(
                        userCoupon.id.as("userCouponId"),
                        coupon.id.as("couponId"),
                        userCoupon.userId.as("userId"),
                        coupon.couponName,
                        coupon.discount,
                        coupon.startedDate,
                        coupon.endedDate,
                        userCoupon.used.as("used")
                ))
                .from(userCoupon)
                .join(coupon)
                .on(coupon.id.eq(userCoupon.couponId))
                .where(userCoupon.userId.eq(userId))
                .fetch();
    }

    @Override
    public List<CouponQuery.OwnedCoupon> findUserCouponsByIds(List<Long> userCouponIds) {
        return queryFactory
                .select(new QCouponQuery_OwnedCoupon(
                        userCoupon.id.as("userCouponId"),
                        coupon.id.as("couponId"),
                        userCoupon.userId.as("userId"),
                        coupon.couponName,
                        coupon.discount,
                        coupon.startedDate,
                        coupon.endedDate,
                        userCoupon.used.as("used")
                ))
                .from(userCoupon)
                .join(coupon)
                .on(coupon.id.eq(userCoupon.couponId))
                .where(userCoupon.id.in(userCouponIds))
                .fetch();
    }

    @Override
    public List<UserCoupon> findUserCouponsByExpiredDate(LocalDateTime expiredDate) {
        return userCouponJpaRepository.findExpiredUserCoupons(expiredDate);
    }
}
