package catholic.ac.kr.secureuserapp.model.dto;

import java.math.BigDecimal;

public record SuggestBookFromCartDTO(
        Long bookId,
        String title,
        BigDecimal price,
        BigDecimal salePrice,
        String imgUrl
) {
}
