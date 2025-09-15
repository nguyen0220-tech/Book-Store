package catholic.ac.kr.secureuserapp.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class OrderDTO {
    private long orderId;

    private long userId;

    private String orderUsername;

    private BigDecimal totalDefaultPrice;

    private double totalPrice; //tong gia sau khi da giam gia

    private BigDecimal totalDiscount; //tong giam gia

    private BigDecimal pointHoard;

    private BigDecimal pointUsage;

    private Timestamp orderDate;

    private String orderStatus;

    private List<OrderItemDTO> items;

    private String shippingAddress;

    private String recipientName;

    private String recipientPhone;

    private String couponCode;

    private String note;

    private boolean confirmed;

    private boolean deleted;

    private LocalDateTime expiryCancel;
}
