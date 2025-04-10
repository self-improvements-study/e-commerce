package kr.hhplus.be.server.domain.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
@DisplayName("OrderCalculator 테스트")
class OrderCalculatorTest {

    @InjectMocks
    private OrderCalculator calculator;


    @Test
    @DisplayName("성공 - 할인 적용된 총 금액 계산")
    void success1() {
        // given
        OrderCommand.Item item1 = OrderCommand.Item.builder()
                .optionId(1L)
                .price(1000L)
                .quantity(2)
                .discount(200L)
                .build();

        OrderCommand.Item item2 = OrderCommand.Item.builder()
                .optionId(2L)
                .price(2000L)
                .quantity(1)
                .discount(500L)
                .build();

        // when
        long totalPrice = calculator.calculateTotalPrice(List.of(item1, item2));

        // then
        // item1: (1000 - 200) * 2 = 1600
        // item2: (2000 - 500) * 1 = 1500
        assertThat(totalPrice).isEqualTo(3100L);
    }

    @Test
    @DisplayName("성공 - 할인 없는 경우")
    void success2() {
        // given
        OrderCommand.Item item = OrderCommand.Item.builder()
                .optionId(1L)
                .price(1500L)
                .quantity(3)
                .build(); // discount는 null

        // when
        long totalPrice = calculator.calculateTotalPrice(List.of(item));

        // then
        // (1500 - 0) * 3 = 4500
        assertThat(totalPrice).isEqualTo(4500L);
    }

    @Test
    @DisplayName("성공 - 아이템이 없는 경우 0 반환")
    void success3() {
        // when
        long totalPrice = calculator.calculateTotalPrice(List.of());

        // then
        assertThat(totalPrice).isEqualTo(0L);
    }

    @Test
    @DisplayName("성공 - null 할인 처리")
    void success4() {
        // given
        OrderCommand.Item item = OrderCommand.Item.builder()
                .optionId(1L)
                .price(1000L)
                .quantity(1)
                .discount(null)
                .build();

        // when
        long totalPrice = calculator.calculateTotalPrice(List.of(item));

        // then
        assertThat(totalPrice).isEqualTo(1000L);
    }


}