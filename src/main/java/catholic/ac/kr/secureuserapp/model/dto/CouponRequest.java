package catholic.ac.kr.secureuserapp.model.dto;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponRequest {
    @Column(unique = true, nullable = false)
    private String couponCode;

    @Column(nullable = false)
    private BigDecimal discountAmount; //giam gia co dinh

    @Column(nullable = false)
    private boolean percentDiscount; // true nếu giảm theo %, false nếu là số tiền

    @Column(nullable = false)
    private BigDecimal discountPercent; // nếu áp dụng phần trăm giảm

    @Column(nullable = false)
    private BigDecimal minimumAmount;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDateTime expired;

    @Column(nullable = false)
    private boolean usage;

    @Column(nullable = false)
    private int maxUsage;

    @Column(nullable = false)
    private int usageCount;
}
