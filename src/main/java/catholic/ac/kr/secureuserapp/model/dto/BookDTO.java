package catholic.ac.kr.secureuserapp.model.dto;

import lombok.*;

import java.math.BigDecimal;

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
    private int stock;
    private String description;
    private String imgUrl;
    private String categoryName; // view/show
    private Long categoryId; //update/create
}
