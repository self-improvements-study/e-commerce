package kr.hhplus.be.server.domain.point;

import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.test.util.RandomGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@DisplayName("포인트 동시성 테스트")
class PointServiceConcurrencyTest {

    @Autowired
    PointService sut;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointRepository pointRepository;

    @Test
    @DisplayName("포인트 충전 동시성 테스트")
    void success1() throws InterruptedException {
        // given
        User user = RandomGenerator.getFixtureMonkey()
                .giveMeBuilder(User.class)
                .set("id", null)
                .build()
                .sample();

        User saved = userRepository.save(user);

        long initialBalance = 0L;
        long chargeAmount = 10_000L;

        Point point = RandomGenerator.getFixtureMonkey()
                .giveMeBuilder(Point.class)
                .set("id", null)
                .set("userId", saved.getId())
                .set("balance", initialBalance)
                .set("version", 0L)
                .build()
                .sample();

        pointRepository.save(point);

        // when
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        List<Throwable> thrownExceptions = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger successCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    sut.increase(saved.getId(), chargeAmount);
                    successCount.incrementAndGet();
                } catch (Throwable t) {
                    thrownExceptions.add(t);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        List<PointInfo.History> histories = sut.findPointHistories(saved.getId());
        PointInfo.Balance sutBalance = sut.getBalance(saved.getId());

        System.out.println("성공한 스레드 수: " + successCount.get());
        System.out.println("최종 잔액: " + sutBalance.getBalance());

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(thrownExceptions).hasSize(9);
        assertThat(histories).hasSize(successCount.get());
        assertThat(sutBalance.getBalance()).isEqualTo(chargeAmount * successCount.get());
    }

    @Test
    @DisplayName("포인트 사용 동시성 테스트")
    void success2() throws InterruptedException {
        // given
        User user = RandomGenerator.getFixtureMonkey()
                .giveMeBuilder(User.class)
                .set("id", null)
                .build()
                .sample();
        User saved = userRepository.save(user);

        long balance = 100000L;

        Point point = RandomGenerator.getFixtureMonkey()
                .giveMeBuilder(Point.class)
                .set("id", null)
                .set("userId", saved.getId())
                .set("balance", balance)
                .build()
                .sample();

        pointRepository.save(point);

        long amount = 10000L;

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        List<Throwable> thrownExceptions = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger successCount = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    sut.decrease(saved.getId(), amount);
                    successCount.incrementAndGet();
                } catch (Throwable t) {
                    thrownExceptions.add(t);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        List<PointInfo.History> histories = sut.findPointHistories(saved.getId());

        PointInfo.Balance sutBalance = sut.getBalance(saved.getId());

        assertThat(histories).hasSize(threadCount);
        assertThat(sutBalance.getBalance()).isEqualTo(0);
    }
}