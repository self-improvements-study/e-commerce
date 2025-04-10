package kr.hhplus.be.server.domain.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PointHistory 테스트")
class PointHistoryTest {

    @Test
    @DisplayName("Charge 거래 타입으로 PointHistory 생성")
    void success_ofCharge() {
        // given
        long userId = 1L;
        long amount = 100L;

        // when
        PointHistory pointHistory = PointHistory.ofCharge(userId, amount);

        // then
        assertNotNull(pointHistory);
        assertEquals(userId, pointHistory.getUserId());
        assertEquals(amount, pointHistory.getAmount());
        assertEquals(PointHistory.Type.CHARGE, pointHistory.getType());
    }

    @Test
    @DisplayName("Payment 거래 타입으로 PointHistory 생성")
    void success_ofPayment() {
        // given
        long userId = 1L;
        long amount = 50L;

        // when
        PointHistory pointHistory = PointHistory.ofPayment(userId, amount);

        // then
        assertNotNull(pointHistory);
        assertEquals(userId, pointHistory.getUserId());
        assertEquals(amount, pointHistory.getAmount());
        assertEquals(PointHistory.Type.PAYMENT, pointHistory.getType());
    }
}
