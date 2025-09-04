package catholic.ac.kr.secureuserapp.model.dto;

import java.math.BigDecimal;

public record BookPaidMany(
        Long bookId,
        String title,
        BigDecimal price,
        BigDecimal salePrice,
        Long quantity,
        String imgUrl
) {
}
