package catholic.ac.kr.secureuserapp.model.dto.request;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponRequest {
    private String couponCode;

    private BigDecimal discountAmount; //giam gia co dinh

    private Boolean percentDiscount; // true nếu giảm theo %, false nếu là số tiền

    private BigDecimal discountPercent; // nếu áp dụng phần trăm giảm

    private BigDecimal minimumAmount;

    private Boolean active;

    private String description;

    private LocalDateTime expired;

    private Boolean usage;

    @Column(nullable = false)
    private Integer maxUsage;

    @Column(nullable = false)
    private int usageCount;
}
