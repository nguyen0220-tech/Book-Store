package catholic.ac.kr.secureuserapp.model.dto;

import java.math.BigDecimal;

public record UserPaymentAmount(
        String username,
        String userFullName,
        BigDecimal totalAmount
) {
}
