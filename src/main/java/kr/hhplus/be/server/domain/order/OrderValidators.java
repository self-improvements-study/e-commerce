package kr.hhplus.be.server.domain.order;

import kr.hhplus.be.server.common.exception.BusinessError;
import kr.hhplus.be.server.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class OrderValidators {

    public void validateItems(List<OrderCommand.Item> items) {
        if (CollectionUtils.isEmpty(items)) {
            throw new BusinessException(BusinessError.ORDER_ITEMS_EMPTY);
        }

        validateDuplicateOptionIds(items);
        validateDuplicateUserCouponIds(items);
        validateItemQuantities(items);
        validateDiscountPrices(items);
    }

    private void validateDuplicateOptionIds(List<OrderCommand.Item> items) {
        List<Long> optionIds = items.stream()
                .map(OrderCommand.Item::getOptionId)
                .distinct()
                .toList();
        if (optionIds.size() != items.size()) {
            throw new BusinessException(BusinessError.DUPLICATE_OPTION_ID);
        }
    }

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

    private void validateItemQuantities(List<OrderCommand.Item> items) {
        if (items.stream().anyMatch(item -> item.getQuantity() <= 0)) {
            throw new BusinessException(BusinessError.INVALID_ITEM_QUANTITY);
        }
    }

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
