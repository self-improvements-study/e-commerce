package kr.hhplus.be.server.domain.order;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

@Component
public class OrderCalculator {

    public long calculateTotalPrice(List<OrderCommand.Item> items) {
        if (CollectionUtils.isEmpty(items)) {
            return 0L;
        }

        return items.stream()
                .reduce(0L,
                        (acc, cur) -> acc + ((cur.getPrice() - Objects.requireNonNullElse(cur.getDiscount(), 0L)) * cur.getQuantity()),
                        Long::sum);
    }
}
