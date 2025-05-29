package kr.hhplus.be.server.domain.coupon;

import kr.hhplus.be.server.common.exception.BusinessError;
import kr.hhplus.be.server.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CouponService {

    private final CouponRepository couponRepository;

    private final CouponApplyRepository couponApplyRepository;

    private final TransactionTemplate transactionTemplate;

    private final CouponEventPublisher couponEventPublisher;

    @Transactional
    public CouponInfo.CouponActivation addCouponToQueue(CouponCommand.IssuedCoupon command) {

        // 1. 주어진 CouponId로 쿠폰을 조회하고, 없으면 예외를 발생시킴
        Coupon couponById = couponRepository.findCouponById(command.getCouponId())
                .orElseThrow(() -> new BusinessException(BusinessError.COUPON_NOT_FOUND));

        // 2. 쿠폰의 잉여 수량을 확인하여 0 이하일 경우 발급 불가 처리
        if (couponById.getQuantity() <= 0) {
            throw new BusinessException(BusinessError.COUPON_ISSUE_LIMIT_EXCEEDED);  // 발급 한도를 초과한 경우 예외 발생
        }

        // 3. 쿠폰 발급 이벤트 생성. 쿠폰 ID와 사용자 ID를 포함한 이벤트를 생성
        CouponEvent.issued event = CouponEvent.issued.from(couponById.getId(), command.getUserId());

        // 4. 쿠폰 발급 이벤트를 발행하여, 외부 시스템 혹은 다른 서비스에서 이 이벤트를 수신하도록 함
        couponEventPublisher.publish(event);

        // 5. 쿠폰 발급을 완료하고, 발급된 쿠폰의 정보를 반환
        return CouponInfo.CouponActivation.from(command.getUserId(), command.getCouponId());
    }

    @Transactional
    public void issueCoupon(CouponCommand.IssuedCouponBatch command) {

        // 1. 주어진 CouponId로 쿠폰을 조회하고, 없으면 예외를 발생시킴
        Coupon coupon = couponRepository.findCouponById(command.getCouponId())
                .orElseThrow(() -> new BusinessException(BusinessError.COUPON_NOT_FOUND));

        // 2. 쿠폰의 잉여 수량을 확인하여 0 이하일 경우 발급 불가 처리
        if (coupon.getQuantity() <= 0) {
            throw new BusinessException(BusinessError.COUPON_ISSUE_LIMIT_EXCEEDED);
        }

        // 3. 발급할 쿠폰 수를 제한 (쿠폰 수량만큼 유저에게 발급)
        List<CouponCommand.IssuedCoupon> limit = command.getIssuedCouponDetail()
                .stream()
                .limit(coupon.getQuantity())
                .toList();

        // 4. 이미 발급된 유저의 쿠폰 ID를 조회하여 중복 발급을 방지
        Set<Long> existCouponUserIds = couponRepository.findUserCouponsByCouponId(command.couponId)
                .stream()  // 이미 발급된 사용자들의 쿠폰 ID를 가져오기
                .map(UserCoupon::getUserId)
                .collect(Collectors.toSet());

        // 5. 중복되지 않는 유저에게만 새로운 UserCoupon 객체 생성하여 발급 대기 리스트에 추가
        List<UserCoupon> userCoupons = limit
                .stream()
                .filter(v -> !existCouponUserIds.contains(v.getUserId()))  // 이미 발급된 유저는 제외
                .map(v -> UserCoupon.builder()
                        .couponId(v.getCouponId())
                        .userId(v.getUserId())
                        .used(false)
                        .build())
                .toList();

        // 6. 발급된 쿠폰 수만큼 수량 감소
        coupon.decrease(userCoupons.size());

        // 7. 쿠폰 수량을 감소시키고 데이터베이스에 반영
        couponRepository.save(coupon);

        // 8. 새로 생성된 UserCoupon 객체들을 저장하여 발급 처리
        couponRepository.saveAll(userCoupons);
    }

    @Transactional(readOnly = true)
    public List<CouponInfo.AvailableCoupon> findByCouponStatus() {
        // AVAILABLE 상태인 쿠폰들을 조회
        List<Coupon> byCouponStatus = couponRepository.findByCouponStatus(Coupon.Status.AVAILABLE);

        return byCouponStatus.stream()
                .map(CouponInfo.AvailableCoupon::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void issuedCoupon(CouponCommand.Issued command) {

        Coupon byIdForUpdate = couponRepository.findByIdForUpdate(command.couponId, command.getStatus())
                .orElseThrow(() -> new BusinessException(BusinessError.COUPON_NOT_FOUND));

        // 해당 쿠폰에 대해 발급된 유저 수를 조회
        long count = couponRepository.countUserCouponByCouponId(command.getCouponId());

        // 발급된 수가 쿠폰의 최대 발급 수량과 같으면, 더 이상 발급할 수 없으므로 상태 변경
        if (count == byIdForUpdate.getQuantity()) {
            // 쿠폰 발급 상태로 변경
            byIdForUpdate.issued();

            // 상태 변경된 쿠폰을 DB에 저장
            couponRepository.save(byIdForUpdate);

            // 쿠폰 발급 완료
            return;
        }

        Set<String> couponRequestQueue = couponApplyRepository
                .getCouponRequestQueue(byIdForUpdate.getId(), byIdForUpdate.getQuantity());

        // 발급 대기 중인 유저들에 대해 쿠폰을 발급 처리
        for (String userId : couponRequestQueue) {

            // 중복 발급을 방지하기 위해 해당 유저가 이미 발급된 쿠폰인지 확인
            boolean alreadyIssued = couponRepository.existsUserCoupon(Long.parseLong(userId), byIdForUpdate.getId());

            // 이미 발급된 유저라면, 이번 반복에서 해당 유저는 건너뛰고 다음 유저로 넘어감
            if (alreadyIssued) {
                continue; // 중복 발급 방지
            }

            // 쿠폰을 새로 발급하여 유저 쿠폰 정보를 저장
            couponRepository.saveUserCoupon(UserCoupon.builder()
                    .userId(Long.parseLong(userId))
                    .couponId(byIdForUpdate.getId())
                    .used(false)
                    .build()
            );
        }
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

    public void expireCoupons() {
        LocalDateTime now = LocalDateTime.now();

        List<UserCoupon> expiredCoupons = couponRepository.findUserCouponsByExpiredDate(now);

        expiredCoupons.forEach(coupon -> transactionTemplate.execute(status -> {
            coupon.use();
            return couponRepository.saveUserCoupon(coupon);
        }));
    }
}
