package catholic.ac.kr.secureuserapp.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class Coupon {
    @Id
    @Column(name = "coupon_code", length = 10, nullable = false)
    private String couponCode;

    private BigDecimal discountAmount; //giam gia co dinh

    private boolean percentDiscount; // true nếu giảm theo %, false nếu là số tiền

    private BigDecimal discountPercent; // nếu áp dụng phần trăm giảm

    private BigDecimal minimumAmount;

    private boolean active;

    private String description;

    private LocalDateTime expired;

    private boolean usage;

    private int maxUsage;

    private int usageCount;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_coupon",
            joinColumns = @JoinColumn(name = "coupon_code",referencedColumnName = "coupon_code"),
            inverseJoinColumns = @JoinColumn(name = "user_id",referencedColumnName = "id")
    )
    Set<User> users;

//    kiểm tra logic business trong Service như:
//            → expired.isBefore(now)	Đã hết hạn chưa
//→ usageCount < maxUsage	Đủ lượt chưa
//→ user có nằm trong coupon.users không (nếu cần)
//→ Nếu percentDiscount == true thì discountPercent phải hợp lệ (0–100)
}
