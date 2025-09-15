package catholic.ac.kr.secureuserapp.model.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

public record CouponApplyDTO(
        String couponCode,
        BigDecimal discountAmount,
        BigDecimal percentDiscount,
        BigDecimal minAmount,
        String description,
        Timestamp expired
) {
}
