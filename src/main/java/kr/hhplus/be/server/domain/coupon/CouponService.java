package kr.hhplus.be.server.domain.coupon;

import kr.hhplus.be.server.common.exception.BusinessError;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.infrastructure.coupon.CouponQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CouponService {

    private final CouponRepository couponRepository;

    /**
     * 사용자가 쿠폰을 발급받습니다.
     *
     * @param command 쿠폰 발급 요청 정보 (유저 ID, 쿠폰 ID 포함)
     * @return 발급된 유저 쿠폰 정보
     * @throws BusinessException 쿠폰 없음, 재고 부족, 중복 발급 시 예외 발생
     */
    @Transactional
    public CouponInfo.IssuedCoupon issueCoupon(CouponCommand.IssuedCoupon command) {
        Coupon coupon = couponRepository.findCouponById(command.getCouponId())
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
                .map(CouponInfo.OwnedCoupon::from)
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

        List<CouponQuery.OwnedCouponProjection> projections =
                couponRepository.findUserCouponsByIds(userCouponIds);

        return projections.stream()
                .map(CouponInfo.OwnedCoupon::from)
                .toList();
    }


    /**
     * 사용자가 쿠폰을 사용합니다.
     *
     * 1. 요청된 유저 쿠폰 ID로 상세 정보 조회
     * 2. 유효성 검사 (모든 쿠폰이 존재해야 함)
     * 3. 사용 가능한 기간인지 검사
     * 4. 쿠폰 사용 처리 및 저장
     *
     * @param command 쿠폰 사용 요청 정보 (보유한 유저 쿠폰 ID 목록 포함)
     * @throws BusinessException 쿠폰 없음, 유효하지 않은 사용 기간, 이미 사용된 쿠폰 등 예외 발생
     */
    @Transactional
    public void use(CouponCommand.Use command) {
        List<Long> useCouponIds = command.getOwnedCoupons()
                .stream()
                .map(CouponCommand.OwnedCoupon::getUserCouponId)
                .distinct()
                .toList();

        List<CouponInfo.Detail> details = couponRepository.findUserCouponDetailsById(useCouponIds).stream()
                .map(CouponInfo.Detail::from)
                .toList();

        if (CollectionUtils.isEmpty(details) || details.size() != useCouponIds.size()) {
            throw new BusinessException(BusinessError.USER_COUPON_NOT_FOUND);
        }

        List<UserCoupon> userCoupons = couponRepository.findUserCouponsById(useCouponIds);
        if (CollectionUtils.isEmpty(userCoupons) || userCoupons.size() != useCouponIds.size()) {
            throw new BusinessException(BusinessError.USER_COUPON_NOT_FOUND);
        }

        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < details.size(); i++) {
            CouponInfo.Detail detail = details.get(i);
            UserCoupon userCoupon = userCoupons.get(i);

            if (detail.getStartedDate().isAfter(now) || now.isAfter(detail.getEndedDate())) {
                throw new BusinessException(BusinessError.COUPON_EXPIRED);
            }

            userCoupon.use();
        }

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



}
