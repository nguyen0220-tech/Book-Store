package catholic.ac.kr.secureuserapp.model.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderRequest {
    private String shippingAddress;
    private String recipientName;
    private String recipientPhone;
    private String couponCode;
    private String note;
    private BigDecimal usePoint;
}
