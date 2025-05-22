package kr.hhplus.be.server.domain.product;

import org.springframework.data.redis.core.ZSetOperations;

import java.util.Set;

public interface ProductRedisRepository {

    void addTopSellingProductToCache(long productId, Long orderCount);

    Set<ZSetOperations.TypedTuple<String>> getAllTopSellingProductsFromCache();
}
