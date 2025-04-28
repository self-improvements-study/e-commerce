package kr.hhplus.be.server.domain.coupon;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CouponScheduler {

    private final CouponRepository couponRepository;
    private final TransactionTemplate transactionTemplate;

    @Scheduled(cron = "0 0 0 * * ?")  // 매일 자정에 실행
    public void expireCoupons() {
        LocalDateTime now = LocalDateTime.now();

        List<UserCoupon> expiredCoupons = couponRepository.findUserCouponsByExpiredDate(now);

        expiredCoupons.forEach(coupon -> transactionTemplate.execute(status -> {
            coupon.use();
            return couponRepository.saveUserCoupon(coupon);
        }));
    }
}
