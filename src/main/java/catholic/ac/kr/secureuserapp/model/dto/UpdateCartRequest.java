package catholic.ac.kr.secureuserapp.model.dto;

import lombok.Data;

@Data
public class UpdateCartRequest {
    private Long userId;
    private Long bookId;
    private int quantity;
}
