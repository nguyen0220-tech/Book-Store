package catholic.ac.kr.secureuserapp.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
public class BookDetailDTO {
    private Long id;
    private String title;
    private String author;
    private BigDecimal price;
    private BigDecimal salePrice;
    private LocalDate saleExpiry;
    private String description;
    private String imgUrl;
    private String categoryName;
    private Double averageRating = 0.0;
}
