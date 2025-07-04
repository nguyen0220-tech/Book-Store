package catholic.ac.kr.secureuserapp.model.dto;

import lombok.Data;

@Data
public class AddToCartRequest {
    private Long bookId;
    private int quantity;
}
