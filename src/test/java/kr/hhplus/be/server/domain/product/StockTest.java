package kr.hhplus.be.server.domain.product;

import kr.hhplus.be.server.common.exception.BusinessError;
import kr.hhplus.be.server.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Stock 테스트")
class StockTest {

    @Test
    @DisplayName("재고 수량 증가")
    void success_increase() {
        // given
        Stock stock = Stock.builder()
                .id(1L)
                .productOptionId(1L)
                .quantity(100L)
                .build();
        long increaseQuantity = 50L;

        // when
        stock.increase(increaseQuantity);

        // then
        assertEquals(150L, stock.getQuantity());
    }

    @Test
    @DisplayName("재고 수량 증가 시, 수량이 0 이하일 경우 예외 발생")
    void failure_increase_invalidQuantity() {
        // given
        Stock stock = Stock.builder()
                .id(1L)
                .productOptionId(1L)
                .quantity(100L)
                .build();
        long invalidQuantity = -10L;

        // when & then
        assertThatThrownBy(() -> stock.increase(invalidQuantity))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(BusinessError.STOCK_QUANTITY_INVALID.getMessage());
    }

    @Test
    @DisplayName("재고 수량 차감")
    void success_decrease() {
        // given
        Stock stock = Stock.builder()
                .id(1L)
                .productOptionId(1L)
                .quantity(100L)
                .build();
        long decreaseQuantity = 30L;

        // when
        stock.decrease(decreaseQuantity);

        // then
        assertEquals(70L, stock.getQuantity());
    }

    @Test
    @DisplayName("재고 수량 차감 시, 수량이 0 이하일 경우 예외 발생")
    void failure_decrease_invalidQuantity() {
        // given
        Stock stock = Stock.builder()
                .id(1L)
                .productOptionId(1L)
                .quantity(100L)
                .build();
        long invalidQuantity = -10L;

        // when & then
        assertThatThrownBy(() -> stock.decrease(invalidQuantity))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(BusinessError.STOCK_QUANTITY_INVALID.getMessage());
    }

    @Test
    @DisplayName("재고 수량 차감 시, 재고가 부족한 경우 예외 발생")
    void failure_decrease_exceedsStock() {
        // given
        Stock stock = Stock.builder()
                .id(1L)
                .productOptionId(1L)
                .quantity(50L)
                .build();
        long exceedQuantity = 100L;

        // when & then
        assertThatThrownBy(() -> stock.decrease(exceedQuantity))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(BusinessError.STOCK_QUANTITY_EXCEEDED.getMessage());
    }
}
