package catholic.ac.kr.secureuserapp.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class OrderDTO {
    private long orderId;

    private long userId;

    private String orderUsername;

    private double totalPrice;

    private BigDecimal totalDiscount;

    private Timestamp orderDate;

    private String orderStatus;

    private List<OrderItemDTO> items;

    private String shippingAddress;

    private String recipientName;

    private String recipientPhone;

    private String couponCode;
}
