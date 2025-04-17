package kr.hhplus.be.server.domain.point;

import jakarta.persistence.EntityManager;
import kr.hhplus.be.server.common.exception.BusinessError;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.test.util.RandomGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
@DisplayName("포인트서비스 통합 테스트")
class PointServiceIntegrationTest {

    @Autowired
    private PointService sut;

    @Autowired
    private EntityManager entityManager;

    @Nested
    @DisplayName("포인트 충전 테스트")
    class IncreasePointTest {

        @Test
        @DisplayName("포인트 충전 성공 + 이력 확인")
        void success1() {
            // given
            User user = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(User.class)
                    .set("id", null)
                    .build()
                    .sample();
            entityManager.persist(user);

            long balance = 500000L;
            Point point = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Point.class)
                    .set("id", null)
                    .set("userId", user.getId())
                    .set("balance", balance)
                    .build()
                    .sample();
            entityManager.persist(point);

            long amount = RandomGenerator.nextLong(1000, 100000);

            // when
            PointInfo.Increase result = sut.increase(user.getId(), amount);

            // then
            List<PointInfo.History> histories = sut.findPointHistories(user.getId());

            assertThat(result.getUserId()).isEqualTo(user.getId());
            assertThat(result.getBalance()).isEqualTo(balance + amount);
            assertThat(histories).hasSize(1);
            assertThat(histories.get(0).getAmount()).isEqualTo(amount);
            assertThat(histories.get(0).getType()).isEqualTo(PointHistory.Type.CHARGE);
        }

        @Test
        @DisplayName("포인트 충전 실패 - 등록되지 않은 사용자")
        void failure1() {
            // given
            long userId = RandomGenerator.nextPositiveLong(Long.MAX_VALUE);
            long amount = RandomGenerator.nextLong(1000, 100000);

            // then
            assertThatThrownBy(() -> sut.increase(userId, amount))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessError.NO_REGISTERED_USER.getMessage());
        }
    }

    @Nested
    @DisplayName("포인트 차감 테스트")
    class DecreasePointTest {

        @Test
        @DisplayName("포인트 차감 성공 + 이력 확인")
        void success1() {
            // given
            User user = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(User.class)
                    .set("id", null)
                    .build()
                    .sample();
            entityManager.persist(user);

            long balance = 500000L;
            Point point = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Point.class)
                    .set("id", null)
                    .set("userId", user.getId())
                    .set("balance", balance)
                    .build()
                    .sample();
            entityManager.persist(point);

            long amount = RandomGenerator.nextLong(5000, 10000);

            // when
            PointInfo.Decrease result = sut.decrease(user.getId(), amount);
            List<PointInfo.History> histories = sut.findPointHistories(user.getId());

            // then
            assertThat(result.getUserId()).isEqualTo(user.getId());
            assertThat(result.getBalance()).isEqualTo(balance - amount);
            assertThat(histories).hasSize(1);
            assertThat(histories.get(0).getAmount()).isEqualTo(amount);
            assertThat(histories.get(0).getType()).isEqualTo(PointHistory.Type.PAYMENT);
        }

        @Test
        @DisplayName("포인트 차감 실패 - 등록되지 않은 사용자")
        void failure1() {
            // given
            long userId = RandomGenerator.nextPositiveLong(Long.MAX_VALUE);
            long amount = RandomGenerator.nextLong(1000, 100000);

            // when & then
            assertThatThrownBy(() -> sut.decrease(userId, amount))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessError.NO_REGISTERED_USER.getMessage());
        }

        @Test
        @DisplayName("포인트 차감 실패 - 잔액 부족")
        void failure2() {
            // given
            User user = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(User.class)
                    .set("id", null)
                    .build()
                    .sample();
            entityManager.persist(user);

            long balance = 5000L;
            Point point = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Point.class)
                    .set("id", null)
                    .set("userId", user.getId())
                    .set("balance", balance)
                    .build()
                    .sample();
            entityManager.persist(point);

            long amount = RandomGenerator.nextLong(5000, 100000);

            // when
            // then
            assertThatThrownBy(() -> sut.decrease(user.getId(), amount))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessError.POINT_BALANCE_EXCEEDED.getMessage());
        }
    }
}