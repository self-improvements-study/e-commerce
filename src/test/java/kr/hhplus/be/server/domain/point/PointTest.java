package kr.hhplus.be.server.domain.point;

import kr.hhplus.be.server.common.exception.BusinessError;
import kr.hhplus.be.server.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@DisplayName("Point 테스트")
class PointTest {

    @Test
    @DisplayName("increase - 포인트 증가")
    void success_increase() {
        // given
        Point point = Point.builder()
                .userId(1L)
                .balance(500L)
                .build();

        long increaseAmount = 200L;

        // when
        point.increase(increaseAmount);

        // then
        assertEquals(700L, point.getBalance());  // 500 + 200
    }

    @Test
    @DisplayName("increase - 0 이하의 금액으로 증가시도 시 예외 발생")
    void fail_increase_invalidAmount() {
        // given
        Point point = Point.builder()
                .userId(1L)
                .balance(500L)
                .build();

        // when, then
        assertThatThrownBy(() -> point.increase(0L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(BusinessError.POINT_AMOUNT_INCREASE_TOO_SMALL.getMessage());

        assertThatThrownBy(() -> point.increase(-100L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(BusinessError.POINT_AMOUNT_INCREASE_TOO_SMALL.getMessage());
    }

    @Test
    @DisplayName("increase - 최대 포인트 한도 초과 시 예외 발생")
    void fail_increase_exceedsLimit() {
        // given
        Point point = Point.builder()
                .userId(1L)
                .balance(999_900L)
                .build();

        long increaseAmount = 200L;  // MAX limit is 1,000,000

        // when, then
        assertThatThrownBy(() -> point.increase(increaseAmount))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(BusinessError.POINT_AMOUNT_INCREASE_EXCEEDS_LIMIT.getMessage());
    }

    @Test
    @DisplayName("decrease - 포인트 차감")
    void success_decrease() {
        // given
        Point point = Point.builder()
                .userId(1L)
                .balance(500L)
                .build();

        long decreaseAmount = 200L;

        // when
        point.decrease(decreaseAmount);

        // then
        assertEquals(300L, point.getBalance());  // 500 - 200
    }

    @Test
    @DisplayName("decrease - 0 이하의 금액으로 차감 시 예외 발생")
    void fail_decrease_invalidAmount() {
        // given
        Point point = Point.builder()
                .userId(1L)
                .balance(500L)
                .build();

        // when, then
        assertThatThrownBy(() -> point.decrease(0L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(BusinessError.POINT_AMOUNT_DECREASE_TOO_SMALL.getMessage());

        assertThatThrownBy(() -> point.decrease(-100L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(BusinessError.POINT_AMOUNT_DECREASE_TOO_SMALL.getMessage());
    }

    @Test
    @DisplayName("decrease - 잔액 초과 시 예외 발생")
    void fail_decrease_balanceExceeded() {
        // given
        Point point = Point.builder()
                .userId(1L)
                .balance(100L)
                .build();

        long decreaseAmount = 200L;  // balance is 100

        // when, then
        assertThatThrownBy(() -> point.decrease(decreaseAmount))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(BusinessError.POINT_BALANCE_EXCEEDED.getMessage());
    }
}
