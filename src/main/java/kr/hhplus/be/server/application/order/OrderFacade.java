package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.domain.coupon.CouponCommand;
import kr.hhplus.be.server.domain.coupon.CouponInfo;
import kr.hhplus.be.server.domain.coupon.CouponService;
import kr.hhplus.be.server.domain.order.Order;
import kr.hhplus.be.server.domain.order.OrderCommand;
import kr.hhplus.be.server.domain.order.OrderInfo;
import kr.hhplus.be.server.domain.order.OrderService;
import kr.hhplus.be.server.domain.payment.PaymentInfo;
import kr.hhplus.be.server.domain.payment.PaymentService;
import kr.hhplus.be.server.domain.product.ProductCommand;
import kr.hhplus.be.server.domain.product.ProductInfo;
import kr.hhplus.be.server.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
@Component
public class OrderFacade {

    private final ProductService productService;
    private final CouponService couponService;
    private final OrderService orderService;
    private final PaymentService paymentService;


    @Transactional
    public OrderResult.OrderSummary order(OrderCriteria.Detail criteria) {
        List<OrderCriteria.Item> items = criteria.getItems();

        // 1. 주문한 상품 옵션 ID들을 추출
        List<Long> optionIds = items.stream()
                .map(OrderCriteria.Item::getOptionId)
                .toList();

        // 2. 옵션 ID를 기반으로 가격 정보 조회
        List<ProductInfo.PriceOption> productOptions =
                productService.getProductOptionsById(ProductCommand.OptionIds.of(optionIds));

        // 3. 사용자 쿠폰 정보 조회
        Map<Long, CouponInfo.OwnedCoupon> userCouponMap = Collections.emptyMap();
        List<Long> userCouponIds = items.stream()
                .map(OrderCriteria.Item::getUserCouponId)
                .filter(Objects::nonNull)
                .toList();

        if (!CollectionUtils.isEmpty(userCouponIds)) {
            userCouponMap = couponService.findUserCouponsById(CouponCommand.FindUserCoupons.of(userCouponIds))
                    .stream()
                    .collect(toMap(CouponInfo.OwnedCoupon::getUserCouponId, Function.identity()));
        }

        // 4. 실제 도메인 서비스에 넘길 커맨드 데이터 준비
        List<ProductCommand.OptionStock> optionStocks = new ArrayList<>();
        List<OrderCommand.Item> orderItems = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {

            OrderCriteria.Item item = items.get(i);
            ProductInfo.PriceOption option = productOptions.get(i);
            CouponInfo.OwnedCoupon userCoupon = userCouponMap.get(item.getUserCouponId());

            // 4-1. 재고 차감을 위한 옵션 정보 생성
            ProductCommand.OptionStock optionStock = ProductCommand.OptionStock
                    .of(item.getOptionId(), item.getQuantity());optionStocks.add(optionStock);

            // 4-3. 주문 생성을 위한 주문 아이템 정보 생성
            OrderCommand.Item orderItem = OrderCommand.Item
                    .of(item.getOptionId(), option.getPrice(), item.getQuantity(), item.getUserCouponId()
                            , userCoupon == null ? null : userCoupon.getDiscount());

            orderItems.add(orderItem);
        }

        // 5. 재고 차감 수행
        productService.decreaseStockQuantity(ProductCommand.DecreaseStock.of(optionStocks));

        // 7. 주문 생성 요청
        OrderInfo.OrderSummary info = orderService.order(
                OrderCommand.Detail.of(criteria.getUserId(), orderItems));

        return OrderResult.OrderSummary.from(info);
    }

    @Transactional(readOnly = true)
    public List<OrderResult.OrderHistory> getUserOrders(long userId) {
        List<OrderInfo.OrderHistory> info = orderService.findOrdersByUserId(userId);
        return info.stream()
                .map(OrderResult.OrderHistory::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResult.FindDetail getUserOrderDetail(long orderId) {

        // 주문 내역 조회 (주문 상태, 총 금액, 주문된 상품들 등)
        OrderInfo.OrderHistory orderInfo = orderService.findOrderByOrderId(orderId);

        PaymentInfo.PaymentSummary paymentInfo = null;

        // 결제가 완료된 상태일 경우만 결제 정보 조회
        if (orderInfo.getStatus().isDetermined()) {
            // 결제 내역 조회 (결제 ID, 결제 금액, 결제 일시 등)
            paymentInfo = paymentService.paymentHistory(orderId);
        }

        // 주문 정보와 결제 정보를 결합하여 DTO 로 반환
        return OrderResult.FindDetail.from(orderInfo, paymentInfo);
    }

}
