package kr.hhplus.be.server.domain.order;

import kr.hhplus.be.server.common.exception.BusinessError;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.order.Order.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Order 테스트")
class OrderTest {

    @Nested
    @DisplayName("success 메서드 테스트")
    class SuccessMethod {

        @Test
        @DisplayName("성공: 상태가 PAYMENT_WAITING인 경우")
        void success1() {
            // given
            Order order = Order.builder()
                    .userId(1L)
                    .orderDate(LocalDateTime.now())
                    .status(Status.PAYMENT_WAITING)
                    .totalPrice(10000L)
                    .build();

            // when
            order.success();

            // then
            assertThat(order.getStatus()).isEqualTo(Status.SUCCESS);
        }

        @Test
        @DisplayName("실패: 상태가 PAYMENT_WAITING이 아닌 경우")
        void failure1() {
            // given
            Order order = Order.builder()
                    .userId(1L)
                    .orderDate(LocalDateTime.now())
                    .status(Status.SUCCESS)
                    .totalPrice(10000L)
                    .build();

            // expect
            assertThatThrownBy(order::success)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(BusinessError.INVALID_ORDER_STATUS.getMessage());

        }
    }

    @Nested
    @DisplayName("cancel 메서드 테스트")
    class CancelMethod {

        @Test
        @DisplayName("성공: 상태가 PAYMENT_WAITING인 경우")
        void success1() {
            // given
            Order order = Order.builder()
                    .userId(1L)
                    .orderDate(LocalDateTime.now())
                    .status(Status.PAYMENT_WAITING)
                    .totalPrice(10000L)
                    .build();

            // when
            order.cancel();

            // then
            assertThat(order.getStatus()).isEqualTo(Status.CANCELLED);
        }

        @Test
        @DisplayName("실패: 상태가 PAYMENT_WAITING이 아닌 경우")
        void failure1() {
            // given
            Order order = Order.builder()
                    .userId(1L)
                    .orderDate(LocalDateTime.now())
                    .status(Status.CANCELLED)
                    .totalPrice(10000L)
                    .build();

            // expect
            assertThatThrownBy(order::cancel)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(BusinessError.INVALID_ORDER_STATUS.getMessage());
        }
    }
}
