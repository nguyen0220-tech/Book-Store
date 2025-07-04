package catholic.ac.kr.secureuserapp.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
public class CouponDTO {
    private String couponCode;

    private BigDecimal discountAmount; //giam gia co dinh

    private boolean percentDiscount; // true nếu giảm theo %, false nếu là số tiền

    private BigDecimal discountPercent; // nếu áp dụng phần trăm giảm

    private BigDecimal minimumAmount;

    private boolean active;

    private String description;

    private LocalDateTime expired;

    private int maxUsage;

    private int usageCount;

    private boolean usage;

}
