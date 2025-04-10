package kr.hhplus.be.server.domain.coupon;

import kr.hhplus.be.server.common.exception.BusinessError;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.infrastructure.coupon.CouponQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CouponService 테스트")
class CouponServiceTest {

    @InjectMocks
    private CouponService couponService;

    @Mock
    private CouponRepository couponRepository;

    @Nested
    @DisplayName("쿠폰 발급")
    class IssueCouponTest {

        @Test
        @DisplayName("정상적으로 쿠폰을 발급한다")
        void success1() {
            // given
            long userId = 1L;
            long couponId = 10L;
            Coupon coupon = Coupon.builder()
                    .id(couponId)
                    .couponName("10% 할인")
                    .discount(1000L)
                    .quantity(10L)
                    .startedDate(LocalDateTime.now().minusDays(1))
                    .endedDate(LocalDateTime.now().plusDays(1))
                    .build();

            UserCoupon userCoupon = UserCoupon.builder()
                    .id(100L)
                    .userId(userId)
                    .couponId(couponId)
                    .used(false)
                    .build();

            CouponCommand.IssuedCoupon command = CouponCommand.IssuedCoupon.builder()
                    .userId(userId)
                    .couponId(couponId)
                    .build();

            when(couponRepository.findCouponById(couponId)).thenReturn(Optional.of(coupon));
            when(couponRepository.existsUserCoupon(userId, couponId)).thenReturn(false);
            when(couponRepository.saveUserCoupon(any(UserCoupon.class))).thenReturn(userCoupon);

            // when
            CouponInfo.IssuedCoupon result = couponService.issueCoupon(command);

            // then
            assertThat(result.getUserCouponId()).isEqualTo(userCoupon.getId());
            assertThat(result.getCouponId()).isEqualTo(coupon.getId());
            assertThat(result.getCouponName()).isEqualTo(coupon.getCouponName());
        }

        @Test
        @DisplayName("쿠폰이 존재하지 않으면 예외 발생")
        void failure1() {
            // given
            long userId = 1L;
            long couponId = 10L;
            CouponCommand.IssuedCoupon command = CouponCommand.IssuedCoupon.builder()
                    .userId(userId)
                    .couponId(couponId)
                    .build();

            when(couponRepository.findCouponById(couponId)).thenReturn(Optional.empty());

            // expect
            assertThatThrownBy(() -> couponService.issueCoupon(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(BusinessError.COUPON_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("쿠폰이 이미 발급된 경우 예외 발생")
        void failure2() {
            long userId = 1L;
            long couponId = 10L;
            Coupon coupon = Coupon.builder()
                    .id(couponId)
                    .couponName("10% 할인")
                    .discount(1000L)
                    .quantity(10L)
                    .startedDate(LocalDateTime.now().minusDays(1))
                    .endedDate(LocalDateTime.now().plusDays(1))
                    .build();

            CouponCommand.IssuedCoupon command = CouponCommand.IssuedCoupon.builder()
                    .userId(userId)
                    .couponId(couponId)
                    .build();

            when(couponRepository.findCouponById(couponId)).thenReturn(Optional.of(coupon));
            when(couponRepository.existsUserCoupon(userId, couponId)).thenReturn(true);

            assertThatThrownBy(() -> couponService.issueCoupon(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(BusinessError.COUPON_ALREADY_ISSUED.getMessage());
        }
    }

    @Nested
    @DisplayName("사용자 쿠폰 전체 조회")
    class FindUserCouponsTest {
        @Test
        @DisplayName("사용자의 모든 쿠폰을 조회한다")
        void success1() {
            long userId = 1L;
            CouponQuery.OwnedCouponProjection projection = mock(CouponQuery.OwnedCouponProjection.class);
            when(couponRepository.findAllOwnedCouponsByUserId(userId))
                    .thenReturn(List.of(projection));

            List<CouponInfo.OwnedCoupon> results = couponService.findUserCoupons(userId);

            assertThat(results).hasSize(1);
        }
    }

    @Nested
    @DisplayName("쿠폰 ID 목록으로 사용자 쿠폰 조회")
    class FindUserCouponsByIdTest {
        @Test
        @DisplayName("ID 목록에 해당하는 사용자 쿠폰 조회")
        void success1() {
            List<Long> ids = List.of(1L, 2L);
            CouponQuery.OwnedCouponProjection projection1 = mock(CouponQuery.OwnedCouponProjection.class);
            CouponQuery.OwnedCouponProjection projection2 = mock(CouponQuery.OwnedCouponProjection.class);

            when(couponRepository.findUserCouponsByIds(ids)).thenReturn(List.of(projection1, projection2));

            List<CouponInfo.OwnedCoupon> results =
                    couponService.findUserCouponsById(CouponCommand.FindUserCoupons.of(ids));

            assertThat(results).hasSize(2);
        }
    }

    @Nested
    @DisplayName("쿠폰 사용")
    class UseCouponTest {
        @Test
        @DisplayName("사용 가능한 쿠폰을 사용 처리한다")
        void success1() {
            // given
            long userCouponId = 1L;

            CouponQuery.DetailProjection projection = mock(CouponQuery.DetailProjection.class);
            when(projection.getUserCouponId()).thenReturn(userCouponId);
            when(projection.getCouponId()).thenReturn(101L);
            when(projection.getUserId()).thenReturn(1L);
            when(projection.getStartedDate()).thenReturn(LocalDateTime.now().minusDays(1));
            when(projection.getEndedDate()).thenReturn(LocalDateTime.now().plusDays(1));
            when(projection.isUsed()).thenReturn(false);

            UserCoupon userCoupon = UserCoupon.builder()
                    .id(userCouponId)
                    .used(false)
                    .build();

            List<Long> userCouponIds = List.of(userCouponId);

            when(couponRepository.findUserCouponDetailsById(userCouponIds)).thenReturn(List.of(projection));
            when(couponRepository.findUserCouponsById(userCouponIds)).thenReturn(List.of(userCoupon));

            // when
            couponService.use(CouponCommand.Use.builder()
                    .ownedCoupons(List.of(CouponCommand.OwnedCoupon.builder()
                            .userCouponId(userCouponId)
                            .build()))
                    .build());

            // then
            assertThat(userCoupon.isUsed()).isTrue();
        }

        @Test
        @DisplayName("쿠폰이 존재하지 않으면 예외 발생")
        void failure1() {
            // given
            long userCouponId = 1L;

            when(couponRepository.findUserCouponDetailsById(List.of(userCouponId)))
                    .thenReturn(List.of());

            CouponCommand.Use command = CouponCommand.Use.builder()
                    .ownedCoupons(List.of(CouponCommand.OwnedCoupon.builder()
                            .userCouponId(userCouponId)
                            .build()))
                    .build();

            // expect
            assertThatThrownBy(() -> couponService.use(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(BusinessError.USER_COUPON_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("이미 사용한 쿠폰이면 예외 발생")
        void failure2() {
            long userCouponId = 1L;
            CouponQuery.DetailProjection projection = mock(CouponQuery.DetailProjection.class);

            when(projection.getUserCouponId()).thenReturn(userCouponId);
            when(projection.getCouponId()).thenReturn(101L);
            when(projection.getUserId()).thenReturn(1L);
            when(projection.getStartedDate()).thenReturn(LocalDateTime.now().minusDays(1));
            when(projection.getEndedDate()).thenReturn(LocalDateTime.now().plusDays(1));
            when(projection.isUsed()).thenReturn(true); // 이미 사용함

            when(couponRepository.findUserCouponDetailsById(List.of(userCouponId)))
                    .thenReturn(List.of(projection));

            CouponCommand.Use command = CouponCommand.Use.builder()
                    .ownedCoupons(List.of(CouponCommand.OwnedCoupon.builder()
                            .userCouponId(userCouponId)
                            .build()))
                    .build();

            // expect
            assertThatThrownBy(() -> couponService.use(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(BusinessError.USER_COUPON_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("쿠폰 사용 기간이 아니라면 예외 발생")
        void failure3() {
            long userCouponId = 1L;
            CouponQuery.DetailProjection projection = mock(CouponQuery.DetailProjection.class);

            when(projection.getUserCouponId()).thenReturn(userCouponId);
            when(projection.getCouponId()).thenReturn(101L);
            when(projection.getUserId()).thenReturn(1L);
            when(projection.getStartedDate()).thenReturn(LocalDateTime.now().plusDays(1));
            when(projection.getEndedDate()).thenReturn(LocalDateTime.now().plusDays(10));
            when(projection.isUsed()).thenReturn(false);

            UserCoupon userCoupon = UserCoupon.builder()
                    .id(userCouponId)
                    .used(false)
                    .build();

            when(couponRepository.findUserCouponDetailsById(List.of(userCouponId)))
                    .thenReturn(List.of(projection));
            when(couponRepository.findUserCouponsById(List.of(userCouponId)))
                    .thenReturn(List.of(userCoupon));

            CouponCommand.Use command = CouponCommand.Use.builder()
                    .ownedCoupons(List.of(CouponCommand.OwnedCoupon.builder()
                            .userCouponId(userCouponId)
                            .build()))
                    .build();

            // expect
            assertThatThrownBy(() -> couponService.use(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(BusinessError.COUPON_EXPIRED.getMessage());
        }
    }

    @Nested
    @DisplayName("쿠폰 사용 취소")
    class CancelCouponTest {
        @Test
        @DisplayName("사용한 쿠폰을 취소 처리한다")
        void success1() {
            List<Long> ids = List.of(1L, 2L);
            UserCoupon coupon1 = UserCoupon.builder().id(1L).used(true).build();
            UserCoupon coupon2 = UserCoupon.builder().id(2L).used(true).build();

            when(couponRepository.findUserCouponsById(ids)).thenReturn(List.of(coupon1, coupon2));

            couponService.cancel(CouponCommand.Cancel.of(ids));

            assertThat(coupon1.isUsed()).isFalse();
            assertThat(coupon2.isUsed()).isFalse();
        }

        @Test
        @DisplayName("사용자 쿠폰이 존재하지 않으면 예외 발생")
        void failure1() {
            List<Long> ids = List.of(1L, 2L);
            when(couponRepository.findUserCouponsById(ids)).thenReturn(List.of());

            // expect
            assertThatThrownBy(() -> couponService.cancel(CouponCommand.Cancel.of(ids)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(BusinessError.USER_COUPON_NOT_FOUND.getMessage());
        }
    }
}

