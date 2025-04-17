package kr.hhplus.be.server.domain.order;

import kr.hhplus.be.server.common.exception.BusinessError;
import kr.hhplus.be.server.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderCalculator orderCalculator;
    private final OrderRepository orderRepository;
    private final OrderValidators orderValidators;
    private final OrderExternalClient orderExternalClient;

    /**
     * 주문 ID를 기반으로 주문 내역을 조회합니다.
     *
     * @param orderId 조회할 주문 ID
     * @return 주문 기본 정보 및 주문 항목이 포함된 OrderHistory DTO
     */
    @Transactional(readOnly = true)
    public OrderInfo.OrderHistory findOrderByOrderId(long orderId) {

        // 1. 주문 정보 조회
//        OrderQuery.OrderProjection order = orderRepository.findOrderByOrderId(orderId);
        Order order = orderRepository.findOrderById(orderId)
                .orElseThrow(() -> new BusinessException(BusinessError.ORDER_NOT_FOUND));

        // 2. 주문 항목 조회
        List<OrderQuery.OrderItemProjection> items = orderRepository.findOrderItemByOrderId(orderId);

        // 3. 주문 항목을 OrderItemDetail로 변환
        List<OrderInfo.OrderItemDetail> orderItems = items.stream()
                .map(OrderQuery.OrderItemProjection::to)
                .collect(Collectors.toList());

        // 4. OrderHistory 객체 생성 및 반환
        return OrderInfo.OrderHistory.from(order, orderItems);
    }

    /**
     * 사용자의 주문 요청을 처리합니다.
     *
     * @param command 사용자 ID, 주문 항목, 수량, 쿠폰 정보를 포함한 주문 요청
     * @throws BusinessException 주문 항목이 비어있거나,
     *                           옵션 ID 또는 사용자 쿠폰 ID가 중복되었거나,
     *                           주문 수량이 1개 미만인 경우 예외 발생
     */
    @Transactional
    public OrderInfo.OrderSummary order(OrderCommand.Detail command) {
        long userId = command.getUserId();
        List<OrderCommand.Item> items = command.getItems();

        // 주문 항목 유효성 검사 (옵션 중복, 쿠폰 중복, 수량, 할인 가격 등)
        orderValidators.validateItems(items);

        // 총 주문 금액 계산
        long totalPrice = orderCalculator.calculateTotalPrice(items);

        // 주문 생성
        Order order = Order.builder()
                .userId(userId)
                .orderDate(LocalDateTime.now())
                .status(Order.Status.PAYMENT_WAITING)
                .totalPrice(totalPrice)
                .build();

        // 저장
        Order savedOrder = orderRepository.saveOrder(order);

        // 주문 항목 생성
        List<OrderItem> orderItems = items.stream()
                .map(item -> OrderItem.builder()
                        .orderId(savedOrder.getId())
                        .optionId(item.getOptionId())
                        .originalPrice(item.getPrice())
                        .quantity(item.getQuantity())
                        .userCouponId(item.getUserCouponId())
                        .build())
                .toList();

        List<OrderItem> savedOrderItems = orderRepository.saveOrderItem(orderItems);

        return OrderInfo.OrderSummary.from(savedOrder, savedOrderItems);
    }

    /**
     * 사용자 ID를 기반으로 해당 사용자의 모든 주문 내역을 조회합니다.
     *
     * @param userId 사용자의 ID
     * @return 사용자의 주문 내역 리스트
     */
    @Transactional(readOnly = true)
    public List<OrderInfo.OrderHistory> findOrdersByUserId(long userId) {
        // 1. 사용자의 주문 정보 조회
        List<Order> orders = orderRepository.findOrdersByUserId(userId);

        // 2. 각 주문에 대해 주문 항목을 조회하고, OrderHistory로 변환
        return orders.stream()
                .map(order -> {
                    // 3. 주문 항목 조회
                    List<OrderQuery.OrderItemProjection> items = orderRepository.findOrderItemByOrderId(order.getId());

                    // 4. 주문 항목을 OrderItemDetail로 변환
                    List<OrderInfo.OrderItemDetail> orderItems = items.stream()
                            .map(OrderQuery.OrderItemProjection::to)
                            .collect(Collectors.toList());

                    // 5. OrderHistory 객체 생성 및 반환
                    return OrderInfo.OrderHistory.from(order, orderItems);
                })
                .collect(Collectors.toList());
    }

    /**
     * 결제가 성공한 주문에 대해 주문 상태를 성공 상태로 변경합니다.
     *
     * @param orderId 상태를 변경할 주문의 ID
     * @throws BusinessException 주문이 존재하지 않을 경우 {@link BusinessError#ORDER_NOT_FOUND} 예외 발생
     */
    @Transactional
    public void success(long orderId) {
        // 주문 ID로 주문 정보를 조회한다. 없으면 예외 발생
        Order order = orderRepository.findOrderById(orderId)
                .orElseThrow(() -> new BusinessException(BusinessError.ORDER_NOT_FOUND));

        // 결제 성공 상태로 변경
        order.success();

        // 변경된 주문 상태를 저장
        orderRepository.saveOrder(order);

        // 외부 플랫폼 데이터 전송
        orderExternalClient.sendOrder(order);
    }

    /**
     * 결제가 취소된 주문에 대해 주문 상태를 취소 상태로 변경합니다.
     *
     * @param orderId 상태를 변경할 주문의 ID
     * @throws BusinessException 주문이 존재하지 않을 경우
     */
    @Transactional
    public void cancel(long orderId) {
        // 주문 ID로 주문 정보를 조회한다. 없으면 예외 발생
        Order order = orderRepository.findOrderById(orderId)
                .orElseThrow(() -> new BusinessException(BusinessError.ORDER_NOT_FOUND));

        // 결제 취소 상태로 변경
        order.cancel();

        // 변경된 주문 상태를 저장
        orderRepository.saveOrder(order);
    }


}
