package catholic.ac.kr.secureuserapp.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BookMarkDTO {
    private Long id;
    private Long userId;
    private Long bookId;
    private String title;
    private String author;
    private String description;
    private BigDecimal price;
    private BigDecimal salePrice;
    private String imgUrl;
}
