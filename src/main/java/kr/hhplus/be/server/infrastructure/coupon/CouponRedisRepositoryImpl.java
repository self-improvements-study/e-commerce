package kr.hhplus.be.server.infrastructure.coupon;

import kr.hhplus.be.server.domain.coupon.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
@RequiredArgsConstructor
public class CouponRedisRepositoryImpl implements CouponRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public Boolean addCouponRequestToQueue(Long userId, Long couponId) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        // 발급 요청 시점 (현재 타임스탬프)을 점수로 사용
        double score = System.currentTimeMillis();  // 현재 시각을 점수로 사용하여 선착순 처리

        // 쿠폰 발급 요청을 SortedSet에 추가
        return zSetOps.addIfAbsent("coupon:" + couponId + ":issued", String.valueOf(userId), score);
    }

    @Override
    public Set<String> getCouponRequestQueue(Long couponId, Long quantity) {
        return redisTemplate.opsForZSet()
                .range("coupon:" + couponId + ":issued", 0, quantity - 1);
    }
}
