package catholic.ac.kr.secureuserapp.model.dto;

import lombok.*;

import java.sql.Timestamp;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CartDTO {
    private Long id;
    private Long userId;
    private Timestamp createdAt;
    private List<CartItemDTO> items;

    private double totalPrice;
}
