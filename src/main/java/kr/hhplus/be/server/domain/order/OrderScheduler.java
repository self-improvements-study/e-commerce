package kr.hhplus.be.server.domain.order;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderScheduler {

    private final OrderRepository orderRepository;
    private final TransactionTemplate transactionTemplate;

    @Scheduled(fixedRate = 120000)
    public void expireOrders() {
        LocalDateTime expirableDateTime = LocalDateTime.now().minusMinutes(5);

        List<Order> expiredOrders = orderRepository.findExpiredOrders(expirableDateTime);

        expiredOrders.forEach(order -> transactionTemplate.execute(status -> {
            order.cancel();
            return orderRepository.saveOrder(order);
        }));
    }
}
