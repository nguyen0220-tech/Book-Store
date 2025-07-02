package catholic.ac.kr.secureuserapp.model.dto;

import lombok.Data;

@Data
public class OrderRequest {
    private Long userId;
    private String shippingAddress;
    private String recipientName;
    private String recipientPhone;
}
