package kr.hhplus.be.server.domain.coupon;

import kr.hhplus.be.server.domain.order.Order;
import lombok.Getter;

import java.time.LocalDateTime;

public class CouponEvent {

    @Getter
    public static class issued {

        private Long couponId;

        private Long userId;

        public static issued from(Long couponId, Long userId) {
            issued issued = new issued();

            issued.couponId = couponId;
            issued.userId = userId;

            return issued;
        }

    }

}
