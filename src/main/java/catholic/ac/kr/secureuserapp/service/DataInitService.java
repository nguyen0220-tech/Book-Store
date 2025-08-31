package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.model.entity.Category;
import catholic.ac.kr.secureuserapp.model.entity.Coupon;
import catholic.ac.kr.secureuserapp.model.entity.Role;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.CategoryRepository;
import catholic.ac.kr.secureuserapp.repository.CouponRepository;
import catholic.ac.kr.secureuserapp.repository.RoleRepository;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

//class Khởi tạo ADMIN khi LẦN ĐẦU chạy ứng dụng
@Service
@RequiredArgsConstructor
public class DataInitService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final CouponRepository couponRepository;

    @PostConstruct
    //@PostConstruct: Phương thức addUserWithRoleAdmin() sẽ được gọi tự động sau khi tất cả các phụ thuộc được inject vào bean.
    public void addUserWithRoleAdmin() {
        System.out.println(" Bắt đầu kiểm tra và khởi tạo admin");

        roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    System.out.println("Tạo mới ROLE_USER");
                    return roleRepository.save(Role.builder().name("ROLE_USER").build());
                });

        Role roleAdmin = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> {
                    System.out.println("Tạo mới ROLE_ADMIN");
                    return roleRepository.save(Role.builder().name("ROLE_ADMIN").build());
                });

        if (userRepository.findByUsername("admin").isEmpty()) {
            User user = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("123456"))
                    .enabled(true)
                    .roles(Set.of(roleAdmin))
                    .build();

            userRepository.save(user);
        }else
            System.out.println("admin already exist");


    }

    @PostConstruct
    public void addCategory(){
        List<String> categories = new ArrayList<>();
        categories.add("Manga");
        categories.add("Tiểu thuyết lãng mạn");
        categories.add("Kinh dị");
        categories.add("Hài hước");
        categories.add("Giả tưởng");
        categories.add("Thiếu nhi");
        categories.add("Khoa học-Công nghệ");

        for (String category : categories) {
            categoryRepository.findByName(category)
                    .orElseGet( ()->{
                        System.out.println("DataInitService.addCategory");
                        return categoryRepository.save(Category.builder().name(category).build());
                    });
        }

    }

    @PostConstruct
    public void initWelcomeCoupon(){
        Coupon coupon = new Coupon();
        coupon.setCouponCode("WC_STORE");
        coupon.setPercentDiscount(true);
        coupon.setDiscountPercent(BigDecimal.valueOf(20));
        coupon.setMinimumAmount(BigDecimal.valueOf(1));
        coupon.setActive(true);
        coupon.setDescription("Welcome to Book Store");
        coupon.setExpired(LocalDateTime.now().plusYears(5));
        coupon.setUsage(false);
        coupon.setMaxUsage(1000000);
        coupon.setUsageCount(0);
        couponRepository.findByCouponCode("WC_STORE")
                .orElseGet(()-> couponRepository.save(coupon));


    }
}
