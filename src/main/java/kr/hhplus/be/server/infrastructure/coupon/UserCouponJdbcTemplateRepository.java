package kr.hhplus.be.server.infrastructure.coupon;

import kr.hhplus.be.server.domain.coupon.UserCoupon;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserCouponJdbcTemplateRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public void batchInsert(List<UserCoupon> userCoupons) {
        String sql = """
                INSERT INTO user_coupon (user_id, coupon_id, used, created_date, last_modified_date)
                VALUES (:userId, :couponId, 'N', now(), now())
                """;

        namedParameterJdbcTemplate.batchUpdate(sql, SqlParameterSourceUtils.createBatch(userCoupons));
    }
}
