package kr.hhplus.be.server.domain.point;

import kr.hhplus.be.server.common.exception.BusinessError;
import kr.hhplus.be.server.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PointService {

    private final PointRepository pointRepository;

    /**
     * 사용자 포인트 잔액을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 포인트 잔액 정보
     * @throws BusinessException 등록되지 않은 사용자일 경우 예외 발생
     */
    @Transactional(readOnly = true)
    public PointInfo.Balance getBalance(long userId) {
        return pointRepository.findPointByUserId(userId)
                .map(PointInfo.Balance::from)
                .orElseThrow(() -> new BusinessException(BusinessError.NO_REGISTERED_USER));
    }

    /**
     * 사용자 포인트를 충전합니다.
     *
     * @param userId 사용자 ID
     * @param amount 충전할 포인트 금액
     * @return 충전 후 포인트 정보
     * @throws BusinessException 등록되지 않은 사용자이거나 유효성 검사 실패 시 예외 발생
     */
    @Transactional
    public PointInfo.Increase increase(long userId, long amount) {
        Point point = pointRepository.findPointByUserId(userId)
                .orElseThrow(() -> new BusinessException(BusinessError.NO_REGISTERED_USER));

        point.increase(amount);
        Point saved = pointRepository.save(point);

        PointHistory pointHistory = PointHistory.ofCharge(userId, amount);
        pointRepository.save(pointHistory);

        return PointInfo.Increase.from(saved);
    }

    /**
     * 사용자 포인트를 차감합니다.
     *
     * 1. 사용자 포인트 조회
     * 2. 유효성 검사 (차감 가능 여부)
     * 3. 포인트 감소 후 저장
     * 4. 포인트 사용 이력 저장
     *
     * @param userId 사용자 ID
     * @param amount 차감할 포인트 금액
     * @return 차감 후 포인트 정보
     * @throws BusinessException 등록되지 않은 사용자이거나 유효성 검사 실패 시 예외 발생
     */
    @Transactional
    public PointInfo.Decrease decrease(long userId, long amount) {
        Point point = pointRepository.findPointByUserIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(BusinessError.NO_REGISTERED_USER));

        point.decrease(amount);
        Point saved = pointRepository.save(point);

        PointHistory pointHistory = PointHistory.ofPayment(userId, amount);
        pointRepository.save(pointHistory);

        return PointInfo.Decrease.from(saved);
    }

    /**
     * 사용자의 포인트 사용/충전 이력을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 포인트 이력 리스트
     */
    @Transactional(readOnly = true)
    public List<PointInfo.History> findPointHistories(long userId) {
        return pointRepository.findPointHistoryByUserId(userId)
                .stream()
                .map(PointInfo.History::from)
                .toList();
    }

}
