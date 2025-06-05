package kr.hhplus.be.server.domain.order;

import jakarta.persistence.EntityManager;
import kr.hhplus.be.server.common.exception.BusinessError;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.coupon.Coupon;
import kr.hhplus.be.server.domain.coupon.UserCoupon;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.ProductOption;
import kr.hhplus.be.server.domain.product.Stock;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.test.util.RandomGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
@DisplayName("주문서비스 통합 테스트")
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService sut;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private OrderEventPublisher orderEventPublisher;

    @Nested
    @DisplayName("order 테스트")
    class OrderTest {

        @Test
        @DisplayName("성공 - 정상적인 주문 요청")
        void success1() {
            // given
            User user = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(User.class)
                    .set("id", null)
                    .build()
                    .sample();
            entityManager.persist(user);

            long couponQuantity = 10;

            long discount = 1000L;

            Coupon coupon = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Coupon.class)
                    .set("id", null)
                    .set("quantity", couponQuantity)
                    .set("discount", discount)
                    .build()
                    .sample();
            entityManager.persist(coupon);

            UserCoupon userCoupon = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(UserCoupon.class)
                    .set("id", null)
                    .set("couponId", coupon.getId())
                    .set("userId", user.getId())
                    .set("used", false)
                    .build()
                    .sample();
            entityManager.persist(userCoupon);

            long price = RandomGenerator.nextLong(10000, 100000);

            Product product = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Product.class)
                    .set("id", null)
                    .set("price", price)
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

            OrderCommand.Item e1 = new OrderCommand.Item(
                    option.getId(),
                    product.getPrice(),
                    1,
                    userCoupon.getId(),
                    coupon.getDiscount());

            List<OrderCommand.Item> items = List.of(e1);

            OrderCommand.Detail command = new OrderCommand.Detail(user.getId(), items);

            // when
            OrderInfo.OrderSummary result = sut.order(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(user.getId());
            assertThat(result.getTotalPrice()).isEqualTo(price - discount);
            assertThat(result.getOrderItems()).hasSize(1);

            OrderInfo.OrderItemSummary item1 = result.getOrderItems().get(0);
            assertThat(item1.getOptionId()).isEqualTo(option.getId());
            assertThat(item1.getQuantity()).isEqualTo(1);
        }

        @Test
        @DisplayName("실패 - 주문 항목이 비어 있음")
        void failure_emptyItems() {
            // given
            long userId = RandomGenerator.nextPositiveLong(Long.MAX_VALUE);
            List<OrderCommand.Item> items = List.of();
            OrderCommand.Detail command = new OrderCommand.Detail(userId, items);

            // when & then
            assertThatThrownBy(() -> sut.order(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessError.ORDER_ITEMS_EMPTY.getMessage());
        }

        @Test
        @DisplayName("실패 - 옵션 ID 중복")
        void failure_duplicateOptionIds() {
            // given
            User user = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(User.class)
                    .set("id", null)
                    .build()
                    .sample();
            entityManager.persist(user);

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

            Stock stock = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Stock.class)
                    .set("id", null)
                    .set("productOptionId", option.getId())
                    .build()
                    .sample();
            entityManager.persist(stock);

            OrderCommand.Item e1 = new OrderCommand.Item(option.getId(), 10000L, 1, null, null);
            OrderCommand.Item e2 = new OrderCommand.Item(option.getId(), 20000L, 1, null, null);
            List<OrderCommand.Item> items = List.of(e1, e2);
            OrderCommand.Detail command = new OrderCommand.Detail(user.getId(), items);

            // when & then
            assertThatThrownBy(() -> sut.order(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessError.DUPLICATE_OPTION_ID.getMessage());
        }

        @Test
        @DisplayName("실패 - 사용자 쿠폰 ID 중복")
        void failure_duplicateUserCouponIds() {
            // given
            User user = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(User.class)
                    .set("id", null)
                    .build()
                    .sample();
            entityManager.persist(user);

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

            Stock stock = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Stock.class)
                    .set("id", null)
                    .set("productOptionId", option.getId())
                    .build()
                    .sample();
            entityManager.persist(stock);

            Coupon coupon = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Coupon.class)
                    .set("id", null)
                    .build()
                    .sample();
            entityManager.persist(coupon);

            UserCoupon userCoupon = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(UserCoupon.class)
                    .set("id", null)
                    .set("couponId", coupon.getId())
                    .set("userId", user.getId())
                    .set("used", false)
                    .build()
                    .sample();
            entityManager.persist(userCoupon);

            OrderCommand.Item e1 = new OrderCommand.Item(option.getId(), 0, 1, userCoupon.getId(), null);
            OrderCommand.Item e2 = new OrderCommand.Item(option.getId(), 0, 1, userCoupon.getId(), null);
            List<OrderCommand.Item> items = List.of(e1, e2);
            OrderCommand.Detail command = new OrderCommand.Detail(user.getId(), items);

            // when & then
            assertThatThrownBy(() -> sut.order(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessError.DUPLICATE_USER_COUPON_ID.getMessage());
        }

        @Test
        @DisplayName("실패 - 주문 수량이 1 미만")
        void failure_invalidQuantity() {
            // given
            User user = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(User.class)
                    .set("id", null)
                    .build()
                    .sample();
            entityManager.persist(user);

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

            Stock stock = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Stock.class)
                    .set("id", null)
                    .set("productOptionId", option.getId())
                    .build()
                    .sample();
            entityManager.persist(stock);

            OrderCommand.Item e1 = new OrderCommand.Item(option.getId(),0, 0, null, null);
            List<OrderCommand.Item> items = List.of(e1);
            OrderCommand.Detail command = new OrderCommand.Detail(user.getId(), items);

            // when & then
            assertThatThrownBy(() -> sut.order(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessError.INVALID_ITEM_QUANTITY.getMessage());
        }

        @Test
        @DisplayName("실패 - 할인 금액이 원래 가격보다 크거나 같음")
        void failure_invalidDiscount() {
            // given
            User user = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(User.class)
                    .set("id", null)
                    .build()
                    .sample();
            entityManager.persist(user);

            long price = 1000L;
            Product product = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Product.class)
                    .set("id", null)
                    .set("price", price)
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

            Stock stock = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Stock.class)
                    .set("id", null)
                    .set("productOptionId", option.getId())
                    .build()
                    .sample();
            entityManager.persist(stock);

            long discountPrice = 1000L;
            Coupon coupon = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Coupon.class)
                    .set("id", null)
                    .set("discount", discountPrice)
                    .build()
                    .sample();
            entityManager.persist(coupon);

            UserCoupon userCoupon = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(UserCoupon.class)
                    .set("id", null)
                    .set("couponId", coupon.getId())
                    .set("userId", user.getId())
                    .set("used", false)
                    .build()
                    .sample();
            entityManager.persist(userCoupon);

            OrderCommand.Item e1 = new OrderCommand.Item(
                    option.getId(),
                    product.getPrice(),
                    1,
                    userCoupon.getId(),
                    coupon.getDiscount()
            );

            List<OrderCommand.Item> items = List.of(e1);
            OrderCommand.Detail command = new OrderCommand.Detail(user.getId(), items);

            // when & then
            assertThatThrownBy(() -> sut.order(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessError.INVALID_DISCOUNT_PRICE.getMessage());
        }
    }

    @Nested
    @DisplayName("success 상태 변경 테스트")
    class OrderSuccessTest {

        @Test
        @DisplayName("성공 - 결제 성공 상태로 변경")
        void success1() {

            // given
            User user = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(User.class)
                    .set("id", null)
                    .build()
                    .sample();
            entityManager.persist(user);

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

            Stock stock = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Stock.class)
                    .set("id", null)
                    .set("productOptionId", option.getId())
                    .build()
                    .sample();
            entityManager.persist(stock);

            Order order = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Order.class)
                    .set("id", null)
                    .set("userId", user.getId())
                    .set("status", Order.Status.PAYMENT_WAITING)
                    .build()
                    .sample();
            entityManager.persist(order);

            OrderItem orderItem = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(OrderItem.class)
                    .set("id", null)
                    .set("orderId", order.getId())
                    .set("optionId", option.getId())
                    .build()
                    .sample();
            entityManager.persist(orderItem);


            OrderCommand.Item e1 = new OrderCommand.Item(
                    option.getId(),
                    product.getPrice(),
                    1,
                    null,
                    null
            );

            List<OrderCommand.Item> items = List.of(e1);

            // when
            sut.success(order.getId());

            // then
            OrderInfo.OrderHistory orderByOrderId = sut.findOrderByOrderId(order.getId());
            assertThat(orderByOrderId.getStatus()).isEqualTo(Order.Status.SUCCESS);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 주문 ID")
        void failure1() {
            // given
            long invalidOrderId = RandomGenerator.nextPositiveLong(Long.MAX_VALUE);

            // when & then
            assertThatThrownBy(() -> sut.success(invalidOrderId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessError.ORDER_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("주문 성공 이벤트 발행 테스트")
    class OrderSendEventPublisherTest {

        @Test
        @DisplayName("성공 - 결제 성공 시 이벤트 발행 여부 확인")
        void success1() {

            // given
            User user = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(User.class)
                    .set("id", null)
                    .build()
                    .sample();
            entityManager.persist(user);

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

            Stock stock = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Stock.class)
                    .set("id", null)
                    .set("productOptionId", option.getId())
                    .build()
                    .sample();
            entityManager.persist(stock);

            Order order = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Order.class)
                    .set("id", null)
                    .set("userId", user.getId())
                    .set("status", Order.Status.PAYMENT_WAITING)
                    .build()
                    .sample();
            entityManager.persist(order);

            OrderItem orderItem = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(OrderItem.class)
                    .set("id", null)
                    .set("orderId", order.getId())
                    .set("optionId", option.getId())
                    .build()
                    .sample();
            entityManager.persist(orderItem);


            OrderCommand.Item e1 = new OrderCommand.Item(
                    option.getId(),
                    product.getPrice(),
                    1,
                    null,
                    null
            );

            // when
            sut.success(order.getId());

            // then
            OrderInfo.OrderHistory orderByOrderId = sut.findOrderByOrderId(order.getId());
            assertThat(orderByOrderId.getStatus()).isEqualTo(Order.Status.SUCCESS);

            verify(orderEventPublisher, times(1)).publish(any(OrderEvent.OrderCompleted.class));
        }

        @Test
        @DisplayName("실패 - 잘못된 주문 ID로 이벤트 발행하지 않음")
        void failure1() {
            // given
            long invalidOrderId = RandomGenerator.nextPositiveLong(Long.MAX_VALUE);

            // when & then
            assertThatThrownBy(() -> sut.success(invalidOrderId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessError.ORDER_NOT_FOUND.getMessage());

            verify(orderEventPublisher, times(0)).publish(any(OrderEvent.OrderCompleted.class));
        }
    }

    @Nested
    @DisplayName("cancel 상태 변경 테스트")
    class OrderCancelTest {

        @Test
        @DisplayName("성공 - 결제 취소 상태로 변경")
        void success1() {

            // given
            // given
            User user = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(User.class)
                    .set("id", null)
                    .build()
                    .sample();
            entityManager.persist(user);

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

            Stock stock = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Stock.class)
                    .set("id", null)
                    .set("productOptionId", option.getId())
                    .build()
                    .sample();
            entityManager.persist(stock);

            Order order = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(Order.class)
                    .set("id", null)
                    .set("userId", user.getId())
                    .set("status", Order.Status.PAYMENT_WAITING)
                    .build()
                    .sample();
            entityManager.persist(order);

            OrderItem orderItem = RandomGenerator.getFixtureMonkey()
                    .giveMeBuilder(OrderItem.class)
                    .set("id", null)
                    .set("orderId", order.getId())
                    .set("optionId", option.getId())
                    .build()
                    .sample();
            entityManager.persist(orderItem);


            OrderCommand.Item e1 = new OrderCommand.Item(
                    option.getId(),
                    product.getPrice(),
                    1,
                    null,
                    null
            );

            List<OrderCommand.Item> items = List.of(e1);

            // when
            sut.cancel(order.getId());

            // then
            OrderInfo.OrderHistory orderByOrderId = sut.findOrderByOrderId(order.getId());
            assertThat(orderByOrderId.getStatus()).isEqualTo(Order.Status.CANCELLED);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 주문 ID")
        void failure1() {
            // given
            long invalidOrderId = RandomGenerator.nextPositiveLong(Long.MAX_VALUE);

            // when & then
            assertThatThrownBy(() -> sut.cancel(invalidOrderId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(BusinessError.ORDER_NOT_FOUND.getMessage());
        }
    }
}