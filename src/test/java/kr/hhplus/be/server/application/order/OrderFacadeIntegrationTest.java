package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.domain.order.*;
import kr.hhplus.be.server.domain.product.*;
import kr.hhplus.be.server.test.util.RandomGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@SpringBootTest
@DisplayName("주문파사드 통합 테스트")
class OrderFacadeIntegrationTest {

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private ProductRepository productRepository;

    @MockitoBean
    private OrderEventPublisher orderEventPublisher;

    @Nested
    @DisplayName("주문 성공 이벤트 발행 테스트")
    class OrderCreateEventPublisherTest {

        @Test
        @DisplayName("성공 - 주문 성공 시 이벤트 발행 여부 확인")
        void success1() {

            // given
            Product product = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Product.class)
                    .set("id", null)
                    .set("price", 10000L)
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

            OrderCriteria.Item item = OrderCriteria.Item.builder()
                    .optionId(option.getId())
                    .quantity(1)
                    .build();

            long userId = 1L;
            OrderCriteria.Detail detail = OrderCriteria.Detail.builder()
                    .userId(userId)
                    .items(List.of(item))
                    .build();
            // when
            orderFacade.order(detail);

            // then
            verify(orderEventPublisher, times(1)).publish(any(OrderEvent.CreateOrder.class));
        }
    }

}