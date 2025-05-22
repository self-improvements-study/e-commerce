package kr.hhplus.be.server.infrastructure.product;

import kr.hhplus.be.server.domain.product.ProductRankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class ProductRedisRepositoryImpl implements ProductRankRepository {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void addTopSellingProductToCache(long productId, Long orderCount) {
        String key = "top-selling:" + LocalDate.now().toString().replace("-", "");
        double score = orderCount;
        String value = String.valueOf(productId);

        redisTemplate.opsForZSet().add(key, value, score);
    }

    @Override
    public Set<ZSetOperations.TypedTuple<String>> getAllTopSellingProductsFromCache() {
        String key = "top-selling:" + LocalDate.now().toString().replace("-", "");

        return redisTemplate.opsForZSet().rangeWithScores(key, 0, -1);
    }
}
