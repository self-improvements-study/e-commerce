package kr.hhplus.be.server.domain.coupon;

import java.util.Set;

public interface CouponRedisRepository {

    Boolean addCouponRequestToQueue(Long userId, Long couponId);

    Set<String> getCouponRequestQueue(Long couponId, Long quantity);
}
