package kr.hhplus.be.server.common.redisson;

import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoSettings;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MockitoSettings
@DisplayName("SPelEvaluatorTest 테스트")
class SPelEvaluatorTest {

    @InjectMocks
    private SPelEvaluator spelEvaluator;

    @Nested
    @DisplayName("SpEL 평가 테스트")
    class SPeLExpressionEvaluationTest {

        @Test
        @DisplayName("toOptionIds() 메서드로 SpEL 키 값 추출 테스트")
        void test() {

            // given
            Item item1 = Item.builder()
                    .optionId(1L)
                    .quantity(1)
                    .build();
            Item item2 = Item.builder()
                    .optionId(2L)
                    .quantity(1)
                    .build();
            Item item3 = Item.builder()
                    .optionId(3L)
                    .quantity(1)
                    .build();

            Dummy dummy = Dummy.builder()
                    .userId(1L)
                    .items(List.of(item1, item2, item3))
                    .build();

            String pairFirst = "dummy";

            String keyExpression = "#dummy.toOptionIds()";

            // when
            Object idObject = spelEvaluator.evaluateExpression(pairFirst, dummy, keyExpression);

            // then
            assertThat(idObject)
                    .isNotNull()
                    .isInstanceOf(List.class)
                    .asInstanceOf(InstanceOfAssertFactories.LIST)
                    .hasSize(3)
                    .containsExactly(1L, 2L, 3L);
        }
    }

    @Getter
    @Builder
    public static class Dummy {
        private long userId;
        private List<Item> items;

        public List<Long> toOptionIds() {
            return this.items.stream().map(Item::getOptionId).sorted().toList();
        }
    }

    @Getter
    @Builder
    public static class Item {
        private long optionId;
        private int quantity;
        @Nullable
        private Long userCouponId;
    }

}