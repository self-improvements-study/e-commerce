package kr.hhplus.be.server.common.redisson;

import kr.hhplus.be.server.test.testdouble.FakeDistributedLockService;
import kr.hhplus.be.server.test.util.RandomGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@DisplayName("RedissonLockAspectTest 테스트")
class RedissonLockAspectTest {

    @Autowired
    FakeDistributedLockService fakeDistributedLockService;

    @Nested
    @DisplayName("RedissonLockAspect 의 lock 메서드 테스트")
    class LockMethodTest {

        @Test
        @DisplayName("락 획득 후 메서드 실행")
        void success1() throws Throwable {

            // given
            List<FakeDistributedLockService.Item> items = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(FakeDistributedLockService.Item.class)
                    .setPostCondition(it -> it.getOptionId() > 0)
                    .build()
                    .list()
                    .ofSize(2)
                    .sample();

            FakeDistributedLockService.Dummy dummy = FakeDistributedLockService.Dummy.builder()
                    .items(items)
                    .build();

            List<Long> numbers = new ArrayList<>();
            numbers.add(0L);

            int threadCount = 5;

            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);

            // when
            for (int i = 0; i < threadCount; i++) {
                executorService.execute(() -> {
                    try {
                        fakeDistributedLockService.fakeDistributedLock(dummy, () -> numbers.set(0, numbers.get(0) + 1));
                        successCount.incrementAndGet();
                    } catch (Throwable t) {
                        failureCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executorService.shutdown();

            // then
            assertThat(numbers.get(0)).isEqualTo(5);

        }
    }

}
