package kr.hhplus.be.server.domain.point;

import kr.hhplus.be.server.common.exception.BusinessError;
import kr.hhplus.be.server.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PointService 테스트")
class PointServiceTest {

    @InjectMocks
    private PointService pointService;

    @Mock
    private PointRepository pointRepository;

    @Nested
    @DisplayName("포인트 조회")
    class PointLookupTest {

        @Test
        @DisplayName("포인트를 정상적으로 조회한다.")
        void success() {

            // given
            long userId = 1L;
            Point point = Point.builder()
                    .id(1L)
                    .userId(userId)
                    .balance(10_000L)
                    .build();

            when(pointRepository.findPointByUserId(userId)).thenReturn(Optional.of(point));

            // when
            PointInfo.Balance pointInfo = pointService.getBalance(userId);

            // then
            assertThat(pointInfo).isNotNull();
            assertThat(pointInfo.getUserId()).isEqualTo(userId);
            assertThat(pointInfo.getBalance()).isEqualTo(10_000L);

        }
    }

    @Nested
    @DisplayName("포인트 충전")
    class PointChargeTest {

        @DisplayName("포인트를 정상적으로 충전한다.")
        @ParameterizedTest
        @ValueSource(longs = {10000, 20000, 30000, 40000, 50000, 60000, 70000, 80000, 90000})
        void success(long amount) {

            // given
            long userId = 1L;

            Point point = Point.builder()
                    .id(1L)
                    .userId(userId)
                    .balance(0L)
                    .build();

            when(pointRepository.findPointByUserId(userId)).thenReturn(Optional.of(point));
            when(pointRepository.save(point)).thenReturn(point);

            // when
            PointInfo.Increase charge = pointService.increase(userId, amount);

            // then
            assertThat(charge).isNotNull();
            assertThat(charge.getUserId()).isEqualTo(userId);
            assertThat(charge.getBalance()).isEqualTo(amount);
        }

        @DisplayName("포인트를 음수, 0원 금액의 충전을 실패 시킨다.")
        @ParameterizedTest
        @ValueSource(longs = {0, -10})
        void fail1(long amount) {
            // given
            long userId = 1L;

            Point point = Point.builder()
                    .id(1L)
                    .userId(userId)
                    .balance(0L)
                    .build();

            when(pointRepository.findPointByUserId(userId)).thenReturn(Optional.of(point));

            // when & then
            assertThatThrownBy(() -> pointService.increase(userId, amount))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessError.POINT_AMOUNT_INCREASE_TOO_SMALL.getMessage());
        }
    }

    @Nested
    @DisplayName("포인트 내역 조회")
    class PointHistoryTest {

        @Test
        @DisplayName("유저의 포인트 내역을 정상적으로 충전한다.")
        void success() {

            // given
            long userId = 1L;

            PointHistory pointChargeHistory = PointHistory.builder()
                    .id(1L)
                    .userId(userId)
                    .amount(20_000L)
                    .type(PointHistory.Type.CHARGE)
                    .build();
            PointHistory pointPaymentHistory = PointHistory.builder()
                    .id(1L)
                    .userId(userId)
                    .amount(10_000L)
                    .type(PointHistory.Type.PAYMENT)
                    .build();

            List<PointHistory> pointHistories = List.of(pointChargeHistory, pointPaymentHistory);

            when(pointRepository.findPointHistoryByUserId(userId)).thenReturn(pointHistories);

            // when
            List<PointInfo.History> pointInfo = pointService.findPointHistories(userId);

            // then
            assertThat(pointInfo).hasSize(2);

            PointInfo.History chargeHistory = pointInfo.get(0);
            assertThat(chargeHistory.getUserId()).isEqualTo(userId);
            assertThat(chargeHistory.getAmount()).isEqualTo(20_000L);
            assertThat(chargeHistory.getType()).isEqualTo(PointHistory.Type.CHARGE);

            PointInfo.History paymentHistory = pointInfo.get(1);
            assertThat(paymentHistory.getUserId()).isEqualTo(userId);
            assertThat(paymentHistory.getAmount()).isEqualTo(10_000L);
            assertThat(paymentHistory.getType()).isEqualTo(PointHistory.Type.PAYMENT);
        }
    }

}