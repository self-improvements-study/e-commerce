package kr.hhplus.be.server.domain.coupon;

import kr.hhplus.be.server.common.exception.BusinessError;
import kr.hhplus.be.server.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@ExtendWith(MockitoExtension.class)
@DisplayName("Coupon 테스트")
class CouponTest {

    private Coupon createCoupon(long quantity) {
        return Coupon.builder()
                .couponName("테스트 쿠폰")
                .discount(1000L)
                .quantity(quantity)
                .startedDate(LocalDateTime.now().minusDays(1))
                .endedDate(LocalDateTime.now().plusDays(1))
                .build();
    }

    @Nested
    @DisplayName("decrease 메서드")
    class Decrease {

        @Test
        @DisplayName("성공 - 수량 정상 차감")
        void success1() {
            // given
            Coupon coupon = createCoupon(10L);

            // when
            coupon.decrease(3L);

            // then
            assertThat(coupon.getQuantity()).isEqualTo(7L);
        }

        @Test
        @DisplayName("실패 - 수량이 0 이하인 경우 예외 발생")
        void failure1() {
            // given
            Coupon coupon = createCoupon(5L);

            // expect
            assertThatThrownBy(() -> coupon.decrease(0))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(BusinessError.INVALID_COUPON_QUANTITY.getMessage());
        }

        @Test
        @DisplayName("실패 - 보유 수량보다 많은 수량 요청 시 예외 발생")
         void failure2() {
            // given
            Coupon coupon = createCoupon(2L);

            // expect
            assertThatThrownBy(() -> coupon.decrease(5))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(BusinessError.COUPON_ISSUE_LIMIT_EXCEEDED.getMessage());
        }
    }
}