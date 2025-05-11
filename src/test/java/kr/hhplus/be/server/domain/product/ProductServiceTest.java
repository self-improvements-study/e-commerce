package kr.hhplus.be.server.domain.product;

import kr.hhplus.be.server.common.exception.BusinessError;
import kr.hhplus.be.server.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService 테스트")
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Nested
    @DisplayName("getTopSellingProducts 테스트")
    class TopSellingProductsTest {

        @Test
        @DisplayName("인기 상품을 정상적으로 조회한다.")
        void success1() {
            // given
            ProductQuery.TopSelling projection = new ProductQuery.TopSelling(
                    1L,
                    "인기상품",
                    10L
            );

            LocalDate daysAgo = LocalDate.now().minusDays(3);
            long limit = 5;

            when(productRepository.findTopSellingProducts(daysAgo, limit))
                    .thenReturn(List.of(projection));

            // when
            ProductInfo.ProductSalesData results = productService.getTopSellingProducts(daysAgo, limit);

            // then
            assertThat(results.getList()).hasSize(1);
            ProductInfo.TopSelling topSelling = results.getList().get(0);
            assertThat(topSelling.getProductId()).isEqualTo(1L);
            assertThat(topSelling.getName()).isEqualTo("인기상품");
            assertThat(topSelling.getSalesCount()).isEqualTo(10L);
        }
    }

    @Nested
    @DisplayName("getProductById 테스트")
    class GetProductByIdTest {

        @Test
        @DisplayName("상품 상세 정보를 정상적으로 조회한다.")
        void success1() {
            // given
            long productId = 1L;

            Product product = Product.builder()
                    .id(productId)
                    .name("상품A")
                    .price(10000L)
                    .build();

            ProductQuery.Option option = new ProductQuery.Option(
                    101L,
                    "M",
                    "Black",
                    30L
            );

            when(productRepository.findProductById(productId)).thenReturn(Optional.of(product));
            when(productRepository.findProductOptionsByProductId(productId)).thenReturn(List.of(option));

            // when
            ProductInfo.Detail result = productService.getProductById(productId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getProductId()).isEqualTo(productId);
            assertThat(result.getName()).isEqualTo("상품A");
            assertThat(result.getPrice()).isEqualTo(10000L);
            assertThat(result.getOptions()).hasSize(1);
        }

        @Test
        @DisplayName("존재하지 않는 상품 ID 조회 시 예외를 반환한다.")
        void failure1() {
            // given
            long productId = 999L;
            when(productRepository.findProductById(productId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.getProductById(productId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessError.PRODUCT_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("재고 증가")
    class IncreaseTest {

        @Test
        @DisplayName("재고를 정상적으로 증가시킨다.")
        void success() {
            // given
            Stock stock = Stock.builder()
                    .id(1L)
                    .productOptionId(1L)
                    .quantity(10L)
                    .build();

            // when
            stock.increase(5L);

            // then
            assertThat(stock.getQuantity()).isEqualTo(15L);
        }

        @Test
        @DisplayName("0 이하 수량은 증가에 실패한다.")
        void failure1() {
            // given
            Stock stock = Stock.builder()
                    .id(1L)
                    .productOptionId(1L)
                    .quantity(10L)
                    .build();

            // when & then
            assertThatThrownBy(() -> stock.increase(0L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessError.STOCK_QUANTITY_INVALID.getMessage());

            assertThatThrownBy(() -> stock.increase(-1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessError.STOCK_QUANTITY_INVALID.getMessage());
        }
    }

    @Nested
    @DisplayName("재고 차감")
    class DecreaseTest {

        @Test
        @DisplayName("재고를 정상적으로 차감시킨다.")
        void success() {
            // given
            Stock stock = Stock.builder()
                    .id(1L)
                    .productOptionId(1L)
                    .quantity(10L)
                    .build();

            // when
            stock.decrease(3L);

            // then
            assertThat(stock.getQuantity()).isEqualTo(7L);
        }

        @Test
        @DisplayName("차감 수량이 0 이하일 경우 실패한다.")
        void failure1() {
            // given
            Stock stock = Stock.builder()
                    .id(1L)
                    .productOptionId(1L)
                    .quantity(10L)
                    .build();

            // when & then
            assertThatThrownBy(() -> stock.decrease(0L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessError.STOCK_QUANTITY_INVALID.getMessage());

            assertThatThrownBy(() -> stock.decrease(-5L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessError.STOCK_QUANTITY_INVALID.getMessage());
        }

        @Test
        @DisplayName("보유 재고보다 많은 수량을 차감할 경우 실패한다.")
        void failure2() {
            // given
            Stock stock = Stock.builder()
                    .id(1L)
                    .productOptionId(1L)
                    .quantity(3L)
                    .build();

            // when & then
            assertThatThrownBy(() -> stock.decrease(5L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessError.STOCK_QUANTITY_EXCEEDED.getMessage());
        }
    }

    @Nested
    @DisplayName("상품 옵션 조회")
    class GetProductOptionsByIdTest {

        @Test
        @DisplayName("옵션 ID 목록으로 옵션 정보를 조회한다.")
        void success() {
            // given
            List<Long> optionIds = List.of(101L, 102L);

            ProductQuery.PriceOption option1 = new ProductQuery.PriceOption(
                    101L,
                    10000L,
                    5L
            );

            ProductQuery.PriceOption option2 = new ProductQuery.PriceOption(
                    102L,
                    20000L,
                    10L
            );

            when(productRepository.findProductOptionsById(optionIds))
                    .thenReturn(List.of(option1, option2));

            ProductCommand.OptionIds command = new ProductCommand.OptionIds(optionIds);

            // when
            List<ProductInfo.PriceOption> result = productService.getProductOptionsById(command);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting("optionId").containsExactly(101L, 102L);
            assertThat(result).extracting("price").containsExactly(10000L, 20000L);
            assertThat(result).extracting("stockQuantity").containsExactly(5L, 10L);
        }
    }

}
