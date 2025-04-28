package kr.hhplus.be.server.domain.product;

import jakarta.persistence.EntityManager;
import kr.hhplus.be.server.common.exception.BusinessError;
import kr.hhplus.be.server.common.exception.BusinessException;
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
@DisplayName("상품서비스 통합 테스트")
class ProductServiceIntegrationTest {

    @Autowired
    ProductService sut;

    @Autowired
    private EntityManager entityManager;

    @Nested
    @DisplayName("재고 증가")
    class IncreaseStockQuantityTest {

        @Test
        @DisplayName("성공 - 유효한 옵션 ID로 재고 증가")
        void success1() {

            // given
            Product product = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Product.class)
                    .set("id", null)
                    .build()
                    .sample();
            entityManager.persist(product);

            ProductOption option = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(ProductOption.class)
                    .set("id", null)
                    .set("productId", product.getId())
                    .build()
                    .sample();
            entityManager.persist(option);

            long stockQuantity = 100L;

            Stock stock = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Stock.class)
                    .set("id", null)
                    .set("productOptionId", option.getId())
                    .set("quantity", stockQuantity)
                    .build()
                    .sample();
            entityManager.persist(stock);

            long quantity = RandomGenerator.nextLong(1, 10);

            ProductCommand.IncreaseStock command = new ProductCommand.IncreaseStock(
                    List.of(new ProductCommand.OptionStock(option.getId(), quantity))
            );

            // when
            ProductInfo.IncreaseStock result = sut.increaseStockQuantity(command);
            ProductInfo.Detail productById = sut.getProductById(product.getId());

            // then
            assertThat(result.getOptionStocks()).hasSize(1);
            assertThat(result.getOptionStocks().get(0).getQuantity()).isEqualTo(stockQuantity + quantity);
            assertThat(productById.getOptions().get(0).getStockQuantity()).isEqualTo(stockQuantity + quantity);
        }

        @Test
        @DisplayName("실패: 빈 옵션 ID 목록")
        void failure1() {
            // given
            ProductCommand.IncreaseStock command = new ProductCommand.IncreaseStock(List.of());

            // when
            // then
            assertThatThrownBy(() -> sut.increaseStockQuantity(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(BusinessError.STOCK_OPERATION_EMPTY.getMessage());
        }

        @Test
        @DisplayName("실패: 존재하지 않는 옵션 ID")
        void failure2() {
            // given
            long optionId = RandomGenerator.nextPositiveLong(Long.MAX_VALUE);
            long quantity = RandomGenerator.nextPositiveLong(Long.MAX_VALUE);

            ProductCommand.IncreaseStock command = new ProductCommand.IncreaseStock(
                    List.of(new ProductCommand.OptionStock(optionId, quantity))
            );

            // expect
            assertThatThrownBy(() -> sut.increaseStockQuantity(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessError.STOCK_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("재고 차감")
    class DecreaseStockQuantityTest {

        @Test
        @DisplayName("성공 - 유효한 옵션 ID로 재고 차감")
        void success1() {
            // given
            Product product = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Product.class)
                    .set("id", null)
                    .build()
                    .sample();
            entityManager.persist(product);

            ProductOption option = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(ProductOption.class)
                    .set("id", null)
                    .set("productId", product.getId())
                    .build()
                    .sample();
            entityManager.persist(option);

            long stockQuantity = 100L;

            Stock stock = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Stock.class)
                    .set("id", null)
                    .set("productOptionId", option.getId())
                    .set("quantity", stockQuantity)
                    .build()
                    .sample();
            entityManager.persist(stock);

            long quantity = RandomGenerator.nextLong(1, 10);

            ProductCommand.DecreaseStock command = new ProductCommand.DecreaseStock(
                    List.of(new ProductCommand.OptionStock(option.getId(), quantity))
            );

            // when
            ProductInfo.DecreaseStock result = sut.decreaseStockQuantity(command);
            ProductInfo.Detail productById = sut.getProductById(product.getId());

            // then
            assertThat(result.getOptionStocks()).hasSize(1);
            assertThat(result.getOptionStocks().get(0).getQuantity()).isEqualTo(stockQuantity - quantity);
            assertThat(productById.getOptions().get(0).getStockQuantity()).isEqualTo(stockQuantity - quantity);
        }

        @Test
        @DisplayName("실패 - 재고보다 큰 수량 차감 시도")
        void failure1() {
            // given
            Product product = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Product.class)
                    .set("id", null)
                    .build()
                    .sample();
            entityManager.persist(product);

            ProductOption option = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(ProductOption.class)
                    .set("id", null)
                    .set("productId", product.getId())
                    .build()
                    .sample();
            entityManager.persist(option);

            long stockQuantity = 5L;

            Stock stock = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Stock.class)
                    .set("id", null)
                    .set("productOptionId", option.getId())
                    .set("quantity", stockQuantity)
                    .build()
                    .sample();
            entityManager.persist(stock);

            long quantity = RandomGenerator.nextLong(11, 100);

            ProductCommand.DecreaseStock command = new ProductCommand.DecreaseStock(
                    List.of(new ProductCommand.OptionStock(option.getId(), quantity))
            );

            // expect
            assertThatThrownBy(() -> sut.decreaseStockQuantity(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessError.STOCK_QUANTITY_EXCEEDED.getMessage());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 옵션 ID")
        void failure2() {
            // given
            long optionId = RandomGenerator.nextPositiveLong(Long.MAX_VALUE);
            long quantity = RandomGenerator.nextPositiveLong(Long.MAX_VALUE);

            ProductCommand.DecreaseStock command = new ProductCommand.DecreaseStock(
                    List.of(new ProductCommand.OptionStock(optionId, quantity))
            );

            // expect
            assertThatThrownBy(() -> sut.decreaseStockQuantity(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessError.PRODUCT_STOCK_NOT_FOUND.getMessage());
        }
    }
}