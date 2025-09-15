package catholic.ac.kr.secureuserapp.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateBookRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String author;

    @NotNull
    @DecimalMin(value = "0.0",inclusive = false)
    private BigDecimal price;

    @DecimalMin(value = "0.0",inclusive = false)
    private BigDecimal salePrice;

    private LocalDate saleExpiry;

    @Min(0)
    private int stock;

    @NotBlank
    private String description;

    @NotNull
    private String imgUrl;

    @NotNull
    private Long categoryId;
}
