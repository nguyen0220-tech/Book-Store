package catholic.ac.kr.secureuserapp.model.dto.request;

import lombok.Data;

@Data
public class UpdateCartRequest {
    private Long bookId;
    private int quantity;
}
