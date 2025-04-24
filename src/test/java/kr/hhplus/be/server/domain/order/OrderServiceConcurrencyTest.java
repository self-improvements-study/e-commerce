package kr.hhplus.be.server.domain.order;

import jakarta.persistence.EntityManager;
import kr.hhplus.be.server.domain.product.*;
import kr.hhplus.be.server.test.util.RandomGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@DisplayName("주문서비스 동시성 테스트")
class OrderServiceConcurrencyTest {

    @Autowired
    ProductService sut;

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductRepository productRepository;

    @Nested
    @DisplayName("decreaseStockQuantity 테스트")
    class decreaseStockQuantityTest {

        @Test
        @DisplayName("재고 차감 동시성 테스트")
        void test() throws InterruptedException {
            // given
            Product product = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Product.class)
                    .set("id", null)
                    .build()
                    .sample();
            Product savedProduct = productRepository.saveProduct(product);

            ProductOption option = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(ProductOption.class)
                    .set("id", null)
                    .set("productId", savedProduct.getId())
                    .build()
                    .sample();
            List<ProductOption> savedProductOption = productRepository.saveProductOption(List.of(option));

            long stockQuantity = 5;

            Stock stock = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Stock.class)
                    .set("id", null)
                    .set("productOptionId", savedProductOption.get(0).getId())
                    .set("quantity", stockQuantity)
                    .build()
                    .sample();
            productRepository.saveStocks(List.of(stock));

            int threadCount = 10;

            ProductCommand.DecreaseStock command = ProductCommand.DecreaseStock.of(
                    List.of(ProductCommand.OptionStock.of(option.getId(), 1L))
            );

            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);
            List<Throwable> thrownExceptions = Collections.synchronizedList(new ArrayList<>());

            // when
            for (int i = 0; i < threadCount; i++) {
                executorService.execute(() -> {
                    try {
                        sut.decreaseStockQuantity(command);
                        successCount.incrementAndGet(); // 성공한 요청 카운트
                    } catch (Throwable t) {
                        failureCount.incrementAndGet(); // 실패한 요청 카운트
                        thrownExceptions.add(t); // 예외 저장
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executorService.shutdown();

            // then
            ProductInfo.Detail productById = productService.getProductById(product.getId());

            assertThat(productById).isNotNull();
            assertThat(productById.getOptions().get(0).getStockQuantity()).isEqualTo(0);
        }
    }
}