package kr.hhplus.be.server.domain.order;

import jakarta.persistence.EntityManager;
import kr.hhplus.be.server.test.util.RandomGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
@DisplayName("쿠폰 서비스 스케줄러 테스트")
class OrderSchedulerTest {

    @Autowired
    private OrderScheduler sut;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("주문 취소 처리 스케줄러 테스트")
    void testOrderExpiryScheduler() {
        // given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime freshDateTime = now.minusMinutes(2);
        LocalDateTime staleDateTime = now.minusMinutes(6);

        List<Order> freshOrders = RandomGenerator.getFixtureMonkey()
                .giveMeBuilder(Order.class)
                .set("id", null)
                .set("status", Order.Status.PAYMENT_WAITING)
                .set("orderDate", freshDateTime)
                .build()
                .list()
                .ofMaxSize(100)
                .sample();

        List<Order> staleOrders = RandomGenerator.getFixtureMonkey()
                .giveMeBuilder(Order.class)
                .set("id", null)
                .set("status", Order.Status.PAYMENT_WAITING)
                .set("orderDate", staleDateTime)
                .build()
                .list()
                .ofMaxSize(100)
                .sample();

        freshOrders.forEach(entityManager::persist);
        staleOrders.forEach(entityManager::persist);

        // then
        LocalDateTime expiredDateTime = now.minusMinutes(5);
        List<Order> expiredOrders = orderRepository.findExpiredOrders(expiredDateTime);
        assertThat(expiredOrders).hasSameSizeAs(staleOrders);

        // when
        sut.expireOrders();

        // then
        expiredOrders = orderRepository.findExpiredOrders(expiredDateTime);
        assertThat(expiredOrders).isEmpty();
    }

}
