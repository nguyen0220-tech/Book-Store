package catholic.ac.kr.secureuserapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class OrderItemDTO {
    private Long id;
//    private Long orderId;
    private Long bookId;
    private String title;
    private String imgUrl;
    private int quantity;
    private double price;
    private boolean reviewed;
}
