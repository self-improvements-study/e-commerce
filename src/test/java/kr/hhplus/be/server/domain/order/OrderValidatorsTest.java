package kr.hhplus.be.server.domain.order;

import kr.hhplus.be.server.common.exception.BusinessError;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.order.OrderCommand.Item;
import kr.hhplus.be.server.domain.product.ProductQuery;
import kr.hhplus.be.server.domain.product.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderValidators 테스트")
class OrderValidatorsTest {

    @InjectMocks
    private OrderValidators validators;

    @Mock
    private ProductRepository productRepository;

    @Test
    @DisplayName("성공: 모든 조건을 만족하는 경우")
    void success1() {
        // given
        List<Item> items = List.of(
                Item.builder().optionId(1L).price(1000L).quantity(1).userCouponId(10L).discount(200L).build(),
                Item.builder().optionId(2L).price(1500L).quantity(2).userCouponId(11L).discount(300L).build()
        );

        when(productRepository.findProductOptionsById(anyList()))
                .thenReturn(List.of(
                        new ProductQuery.PriceOption(1L, 1000L, 10),
                        new ProductQuery.PriceOption(2L, 1500L, 5)
                ));


        // when & then
        assertThatCode(() -> validators.validateItems(items)).doesNotThrowAnyException();
    }

    @Nested
    @DisplayName("실패 케이스")
    class FailureCases {

        @Test
        @DisplayName("실패 - 중복된 옵션 ID가 있으면 예외 발생")
        void failure_duplicate_option_ids() {
            List<OrderCommand.Item> items = List.of(
                    OrderCommand.Item.builder().optionId(1L).price(1000L).quantity(1).build(),
                    OrderCommand.Item.builder().optionId(1L).price(1000L).quantity(1).build()
            );

            when(productRepository.findProductOptionsById(anyList()))
                    .thenReturn(List.of(
                            new ProductQuery.PriceOption(1L, 1000L, 10)
                    ));

            assertThatThrownBy(() -> validators.validateItems(items))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(BusinessError.DUPLICATE_OPTION_ID.getMessage());
        }

        @Test
        @DisplayName("실패 - 중복된 쿠폰 ID가 있으면 예외 발생")
        void failure_duplicate_coupon_ids() {
            List<OrderCommand.Item> items = List.of(
                    OrderCommand.Item.builder().optionId(1L).price(1000L).quantity(1).userCouponId(10L).build(),
                    OrderCommand.Item.builder().optionId(2L).price(1000L).quantity(1).userCouponId(10L).build()
            );

            assertThatThrownBy(() -> validators.validateItems(items))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(BusinessError.DUPLICATE_USER_COUPON_ID.getMessage());
        }

        @Test
        @DisplayName("실패 - 수량이 0 이하인 항목이 있으면 예외 발생")
        void failure_invalid_quantity() {
            List<OrderCommand.Item> items = List.of(
                    OrderCommand.Item.builder().optionId(1L).price(1000L).quantity(0).build()
            );

            assertThatThrownBy(() -> validators.validateItems(items))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(BusinessError.INVALID_ITEM_QUANTITY.getMessage());
        }

        @Test
        @DisplayName("실패 - 할인 금액이 가격 이상이면 예외 발생")
        void failure_invalid_discount() {
            List<OrderCommand.Item> items = List.of(
                    OrderCommand.Item.builder().optionId(1L).price(1000L).quantity(1).discount(1000L).build()
            );

            assertThatThrownBy(() -> validators.validateItems(items))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(BusinessError.INVALID_DISCOUNT_PRICE.getMessage());
        }
    }
}
