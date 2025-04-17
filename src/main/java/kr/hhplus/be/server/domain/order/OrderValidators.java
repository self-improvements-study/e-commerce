package kr.hhplus.be.server.domain.order;

import kr.hhplus.be.server.common.exception.BusinessError;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.domain.product.ProductQuery;
import kr.hhplus.be.server.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class OrderValidators {

    private final ProductRepository productRepository;

    public void validateItems(List<OrderCommand.Item> items) {
        if (CollectionUtils.isEmpty(items)) {
            throw new BusinessException(BusinessError.ORDER_ITEMS_EMPTY);
        }

        validateDuplicateUserCouponIds(items);
        validateItemQuantities(items);
        validateDiscountPrices(items);
        validateDuplicateOptionIds(items);
    }

    /**
     * 주문 항목들에 포함된 옵션 ID가 중복되는지 검사합니다.
     * 중복이 있을 경우 예외를 발생시킵니다.
     */
    private void validateDuplicateOptionIds(List<OrderCommand.Item> items) {
        List<Long> optionIds = items.stream()
                .map(OrderCommand.Item::getOptionId)
                .distinct()
                .toList();

        List<ProductQuery.PriceOption> options = productRepository.findProductOptionsById(optionIds);

        if (options.isEmpty()) {
            throw new BusinessException(BusinessError.PRODUCT_NOT_FOUND);
        }

        if (optionIds.size() != items.size()) {
            throw new BusinessException(BusinessError.DUPLICATE_OPTION_ID);
        }
    }

    /**
     * 주문 항목들에 포함된 사용자 쿠폰 ID가 중복되는지 검사합니다.
     * (null 제외) 중복이 있을 경우 예외를 발생시킵니다.
     */
    private void validateDuplicateUserCouponIds(List<OrderCommand.Item> items) {
        List<Long> userCouponIds = items.stream()
                .map(OrderCommand.Item::getUserCouponId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (userCouponIds.size() != items.stream().filter(item -> item.getUserCouponId() != null).count()) {
            throw new BusinessException(BusinessError.DUPLICATE_USER_COUPON_ID);
        }
    }

    /**
     * 주문 항목들의 수량이 모두 1 이상인지 검사합니다.
     * 하나라도 1 미만인 항목이 있을 경우 예외를 발생시킵니다.
     */
    private void validateItemQuantities(List<OrderCommand.Item> items) {
        if (items.stream().anyMatch(item -> item.getQuantity() <= 0)) {
            throw new BusinessException(BusinessError.INVALID_ITEM_QUANTITY);
        }
    }

    /**
     * 할인 금액이 원래 가격보다 크거나 같은 경우를 검사합니다.
     * 할인 금액이 이상한 경우 예외를 발생시킵니다.
     */
    private void validateDiscountPrices(List<OrderCommand.Item> items) {
        boolean hasInvalidDiscount = items.stream()
                .anyMatch(item -> {
                    long price = item.getPrice();
                    long discount = Objects.requireNonNullElse(item.getDiscount(), 0L);
                    return discount >= price;
                });

        if (hasInvalidDiscount) {
            throw new BusinessException(BusinessError.INVALID_DISCOUNT_PRICE);
        }
    }
}
