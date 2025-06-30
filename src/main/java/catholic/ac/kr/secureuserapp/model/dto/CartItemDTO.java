package catholic.ac.kr.secureuserapp.model.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class CartItemDTO {
    private Long id;         // ID của CartItem
    private Long bookId;     // Liên kết Book
    private String title;    // Lấy từ Book
    private int quantity;    // Số lượng chọn
    private String imgUrl;   // Lấy từ Book
    private double price;    // Lấy từ Book (có thể để tính tổng tiền)
}
