package kr.hhplus.be.server.domain.coupon;

import kr.hhplus.be.server.common.exception.BusinessError;
import kr.hhplus.be.server.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CouponService {

    private final CouponRepository couponRepository;

    private final TransactionTemplate transactionTemplate;

    /**
     * 사용자가 쿠폰을 발급받습니다.
     *
     * @param command 쿠폰 발급 요청 정보 (유저 ID, 쿠폰 ID 포함)
     * @return 발급된 유저 쿠폰 정보
     * @throws BusinessException 쿠폰 없음, 재고 부족, 중복 발급 시 예외 발생
     */
    @Transactional
    public CouponInfo.IssuedCoupon issueCoupon(CouponCommand.IssuedCoupon command) {
        Coupon coupon = couponRepository.findCouponByIdForUpdate(command.getCouponId())
                .orElseThrow(() -> new BusinessException(BusinessError.COUPON_NOT_FOUND));

        // 재고 체크
        if (coupon.getQuantity() <= 0) {
            throw new BusinessException(BusinessError.COUPON_ISSUE_LIMIT_EXCEEDED);
        }

        // 중복 발급 체크
        boolean alreadyIssued = couponRepository.existsUserCoupon(command.getUserId(), command.getCouponId());
        if (alreadyIssued) {
            throw new BusinessException(BusinessError.COUPON_ALREADY_ISSUED);
        }

        // 쿠폰 수량 차감
        coupon.decrease(1L);

        // 유저 쿠폰 생성
        UserCoupon savedUserCoupon = couponRepository.saveUserCoupon(command.toEntity());

        return CouponInfo.IssuedCoupon.from(savedUserCoupon, coupon);
    }

    /**
     * 사용자가 보유한 모든 쿠폰 목록을 조회합니다.
     *
     * @param userId 쿠폰을 조회할 사용자 ID
     * @return 사용자가 보유한 쿠폰 리스트
     */
    @Transactional(readOnly = true)
    public List<CouponInfo.OwnedCoupon> findUserCoupons(long userId) {
        return couponRepository.findAllOwnedCouponsByUserId(userId).stream()
                .map(CouponQuery.OwnedCoupon::to)
                .toList();
    }

    /**
     * 특정 userCouponId 목록에 해당하는 쿠폰 정보들을 조회합니다.
     *
     * @param command userCouponId 리스트를 담은 커맨드 객체
     * @return 조회된 사용자 쿠폰 정보 리스트
     */
    @Transactional(readOnly = true)
    public List<CouponInfo.OwnedCoupon> findUserCouponsById(CouponCommand.FindUserCoupons command) {
        List<Long> userCouponIds = command.getUserCouponIds();

        List<CouponQuery.OwnedCoupon> projections =
                couponRepository.findUserCouponsByIds(userCouponIds);

        return projections.stream()
                .map(CouponQuery.OwnedCoupon::to)
                .toList();
    }


    /**
     * 사용자가 쿠폰을 사용합니다.
     *
     * @param command 쿠폰 사용 요청 정보 (보유한 유저 쿠폰 ID 목록 포함)
     * @throws BusinessException 쿠폰 없음, 유효하지 않은 사용 기간, 이미 사용된 쿠폰 등 예외 발생
     */
    @Transactional
    public void use(CouponCommand.Use command) {
        // 사용하려는 쿠폰 ID만 추출 (중복 제거)
        List<Long> useCouponIds = command.getOwnedCoupons()
                .stream()
                .map(CouponCommand.OwnedCoupon::getUserCouponId)
                .distinct()
                .toList();

        // 쿠폰 상세 정보 조회
        List<CouponInfo.OwnedCoupon> details = couponRepository.findUserCouponsByIds(useCouponIds).stream()
                .map(CouponQuery.OwnedCoupon::to)
                .toList();

        // 쿠폰이 존재하지 않거나 일부만 존재하면 예외 발생
        if (CollectionUtils.isEmpty(details) || details.size() != useCouponIds.size()) {
            throw new BusinessException(BusinessError.USER_COUPON_NOT_FOUND);
        }

        // 실제 UserCoupon 엔티티 조회 (상태 변경을 위해)
        List<UserCoupon> userCoupons = couponRepository.findUserCouponsById(useCouponIds);
        if (CollectionUtils.isEmpty(userCoupons) || userCoupons.size() != useCouponIds.size()) {
            throw new BusinessException(BusinessError.USER_COUPON_NOT_FOUND);
        }

        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < details.size(); i++) {
            CouponInfo.OwnedCoupon detail = details.get(i);
            UserCoupon userCoupon = userCoupons.get(i);

            // 쿠폰이 아직 시작되지 않았거나, 이미 기간이 지난 경우 예외
            if (detail.getStartedDate().isAfter(now) || now.isAfter(detail.getEndedDate())) {
                throw new BusinessException(BusinessError.COUPON_EXPIRED);
            }

            // 쿠폰 사용 처리 (상태 변경)
            userCoupon.use();
        }

        // 변경된 UserCoupon 저장 (사용 상태 반영)
        couponRepository.saveUserCoupons(userCoupons);
    }

    /**
     * 사용자가 사용한 쿠폰을 취소합니다.
     *
     * @param command 쿠폰 취소 요청 정보 (보유한 유저 쿠폰 ID 목록 포함)
     * @throws BusinessException 사용자 쿠폰이 존재하지 않거나 수량 불일치 시 예외 발생
     */
    @Transactional
    public void cancel(CouponCommand.Cancel command) {
        List<Long> useCouponIds = command.getOwnedCoupons()
                .stream()
                .map(CouponCommand.OwnedCoupon::getUserCouponId)
                .distinct()
                .toList();

        List<UserCoupon> userCoupons = couponRepository.findUserCouponsById(useCouponIds);
        if (CollectionUtils.isEmpty(userCoupons) || userCoupons.size() != useCouponIds.size()) {
            throw new BusinessException(BusinessError.USER_COUPON_NOT_FOUND);
        }

        userCoupons.forEach(UserCoupon::cancel);

        couponRepository.saveUserCoupons(userCoupons);
    }

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
