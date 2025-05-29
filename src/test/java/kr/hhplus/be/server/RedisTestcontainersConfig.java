package kr.hhplus.be.server;

import com.redis.testcontainers.RedisContainer;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.utility.DockerImageName;

@Configuration
class RedisTestcontainersConfig {

    public static final RedisContainer REDIS_CONTAINER;

    static {
        REDIS_CONTAINER = new RedisContainer(DockerImageName.parse("redis:latest"));
        REDIS_CONTAINER.start();
    }

    @PreDestroy
    public void preDestroy() {
        if (REDIS_CONTAINER.isRunning()) {
            REDIS_CONTAINER.stop();
        }
    }
}
