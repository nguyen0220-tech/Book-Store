package catholic.ac.kr.secureuserapp.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class BookDTO {
    private Long id;
    private String title;
    private String author;
    private BigDecimal price;
    private BigDecimal salePrice;
    private LocalDate saleExpiry;
    private int stock;
    private String description;
    private String imgUrl;
    private String categoryName; // view/show
    private Long categoryId; //update/create
    private boolean isDeleted;
}
