package kr.hhplus.be.server.domain.order;

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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService 테스트")
class OrderServiceTest {

    @Mock
    OrderCalculator orderCalculator;

    @Mock
    OrderRepository orderRepository;

    @Mock
    OrderValidators orderValidators;

    @InjectMocks
    OrderService orderService;

    @Mock
    OrderExternalClient orderExternalClient;

    @Nested
    @DisplayName("findOrderByOrderId 테스트")
    class FindOrderByOrderIdTest {

        @Test
        @DisplayName("주문 내역 조회")
        void success1() {
            // given
            long orderId = 1L;
            Order order = Order.builder()
                    .id(orderId)
                    .status(Order.Status.PAYMENT_WAITING)
                    .totalPrice(5000L)
                    .build();

            OrderQuery.OrderItemProjection item = new OrderQuery.OrderItemProjection(
                    10L,
                    1L,
                    "상품",
                    "M",
                    "Red",
                    2,
                    100L,
                    5000L,
                    LocalDateTime.now()
            );

            when(orderRepository.findOrderById(orderId)).thenReturn(Optional.of(order));
            when(orderRepository.findOrderItemByOrderId(orderId)).thenReturn(List.of(item));

            // when
            OrderInfo.OrderHistory result = orderService.findOrderByOrderId(orderId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getOrderId()).isEqualTo(orderId);
            assertThat(result.getStatus()).isEqualTo(Order.Status.PAYMENT_WAITING);
            assertThat(result.getTotalAmount()).isEqualTo(5000L);
            assertThat(result.getOrderItems()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("order 테스트")
    class OrderTest {

        @Test
        @DisplayName("주문 성공")
        void success1() {
            // given
            long userId = 1L;
            OrderCommand.Item item = OrderCommand.Item.builder()
                    .optionId(10L)
                    .price(5000L)
                    .quantity(2)
                    .userCouponId(100L)
                    .build();

            OrderCommand.Detail command = OrderCommand.Detail.builder()
                    .userId(userId)
                    .items(List.of(item))
                    .build();

            when(orderCalculator.calculateTotalPrice(command.getItems())).thenReturn(10000L);

            Order order = Order.builder()
                    .id(1L)
                    .userId(userId)
                    .orderDate(LocalDateTime.now())
                    .status(Order.Status.PAYMENT_WAITING)
                    .totalPrice(10000L)
                    .build();

            OrderItem orderItem = OrderItem.builder()
                    .orderId(order.getId())
                    .optionId(10L)
                    .originalPrice(5000L)
                    .quantity(2)
                    .userCouponId(100L)
                    .build();

            when(orderRepository.saveOrder(any(Order.class))).thenReturn(order);
            when(orderRepository.saveOrderItem(anyList())).thenReturn(List.of(orderItem));

            // when
            OrderInfo.OrderSummary result = orderService.order(command);

            // then
            assertThat(result.getOrderId()).isEqualTo(order.getId());
            assertThat(result.getTotalPrice()).isEqualTo(10000L);
            assertThat(result.getOrderItems()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("findOrdersByUserId 테스트")
    class FindOrdersByUserIdTest {

        @Test
        @DisplayName("사용자 주문 목록 조회")
        void success1() {
            // given
            long userId = 1L;
            Order order = Order.builder()
                    .id(1L)
                    .status(Order.Status.PAYMENT_WAITING)
                    .totalPrice(5000L)
                    .build();

            OrderQuery.OrderItemProjection item = new OrderQuery.OrderItemProjection(
                    10L,
                    1L,
                    "상품",
                    "M",
                    "Red",
                    2,
                    100L,
                    5000L,
                    LocalDateTime.now()
            );

            when(orderRepository.findOrdersByUserId(userId)).thenReturn(List.of(order));
            when(orderRepository.findOrderItemByOrderId(order.getId())).thenReturn(List.of(item));

            // when
            List<OrderInfo.OrderHistory> result = orderService.findOrdersByUserId(userId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getOrderItems()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("success 테스트")
    class SuccessTest {

        @Test
        @DisplayName("결제 성공 처리")
        void success1() {
            // given
            Order order = Order.builder()
                    .id(1L)
                    .userId(1L)
                    .orderDate(LocalDateTime.now())
                    .status(Order.Status.PAYMENT_WAITING)
                    .totalPrice(10000L)
                    .build();

            when(orderRepository.findOrderById(order.getId())).thenReturn(Optional.of(order));

            // when
            orderService.success(order.getId());

            // then
            assertThat(order.getStatus()).isEqualTo(Order.Status.SUCCESS);
            verify(orderRepository).saveOrder(order);
        }

        @Test
        @DisplayName("존재하지 않는 주문")
        void failure1() {
            // given
            long orderId = 999L;
            when(orderRepository.findOrderById(orderId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.success(orderId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(BusinessError.ORDER_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("cancel 테스트")
    class CancelTest {

        @Test
        @DisplayName("결제 취소 처리")
        void success1() {
            // given
            Order order = Order.builder()
                    .id(1L)
                    .userId(1L)
                    .orderDate(LocalDateTime.now())
                    .status(Order.Status.PAYMENT_WAITING)
                    .totalPrice(10000L)
                    .build();

            when(orderRepository.findOrderById(order.getId())).thenReturn(Optional.of(order));

            // when
            orderService.cancel(order.getId());

            // then
            assertThat(order.getStatus()).isEqualTo(Order.Status.CANCELLED);
            verify(orderRepository).saveOrder(order);
        }
    }
}
