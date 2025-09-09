package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.Status.NotificationType;
import catholic.ac.kr.secureuserapp.exception.ResourceNotFoundException;
import catholic.ac.kr.secureuserapp.mapper.CouponMapper;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.CouponDTO;
import catholic.ac.kr.secureuserapp.model.dto.CouponRequest;
import catholic.ac.kr.secureuserapp.model.entity.Coupon;
import catholic.ac.kr.secureuserapp.model.entity.Notification;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.CouponRepository;
import catholic.ac.kr.secureuserapp.repository.NotificationRepository;
import catholic.ac.kr.secureuserapp.repository.OrderRepository;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final OrderRepository orderRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<CouponDTO>> getAllCoupons() {
        List<Coupon> coupons = couponRepository.findAll();

        List<CouponDTO> couponDTOS = coupons.stream()
                .map(CouponMapper::toCouponDTO)
                .toList();

        return ApiResponse.success("All coupon", couponDTOS);

    }

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CouponDTO> getCouponByCode(String code) {
        Coupon coupon = couponRepository.findByCouponCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon Not Found"));

        CouponDTO couponDTO = CouponMapper.toCouponDTO(coupon);

        return ApiResponse.success("Coupon found", couponDTO);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ApiResponse<List<CouponDTO>> getCouponByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));

        List<Coupon> coupons = couponRepository.findByUserId(user.getId());

        List<Coupon> validCoupons = new ArrayList<>();
        for (Coupon coupon : coupons) {
            if (coupon.getExpired().isBefore(LocalDateTime.now()) ||
                    coupon.getMaxUsage() == coupon.getUsageCount()) {
                coupon.setActive(false);
                couponRepository.save(coupon);
            } else {
                validCoupons.add(coupon);
            }
        }

        boolean exitsCouponWelcome = orderRepository.existsCouponWelcome(user.getId());

        if (exitsCouponWelcome) {
            validCoupons.removeIf(c -> "WC_STORE".equals(c.getCouponCode()));
        }

        List<CouponDTO> couponDTOS = CouponMapper.toCouponDTO(validCoupons);

        return ApiResponse.success("Coupon found with userid " + userId, couponDTOS);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<CouponDTO>> getAndFilterCoupons(boolean active) {
        List<Coupon> coupons = couponRepository.findAndFilterByActive(active);

        List<CouponDTO> couponDTOS = CouponMapper.toCouponDTO(coupons);

        return ApiResponse.success("Find and Filter coupons", couponDTOS);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CouponDTO> createCoupon(CouponRequest request) {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        String randomCode = IntStream.range(0, 10)
                .mapToObj(c -> String.valueOf(chars.charAt(random.nextInt(chars.length()))))
                .collect(Collectors.joining());

        request.setCouponCode(randomCode.toUpperCase());

        Coupon coupon = CouponMapper.toCoupon(request);

        couponRepository.save(coupon);

        CouponDTO couponDTO = CouponMapper.toCouponDTO(coupon);

        return ApiResponse.success("Coupon Created", couponDTO);
    }

    public void giveCouponToUserBirthDate() {
        LocalDate today = LocalDate.now();

        DateTimeFormatter formatterDay = DateTimeFormatter.ofPattern("dd");
        DateTimeFormatter formatterMonth = DateTimeFormatter.ofPattern("MM");


        String todayString = today.format(formatterDay);
        String thisMonth = today.format(formatterMonth);

        List<User> usersBirthDate = userRepository.findByMonthOfBirthAndDayOfBirth(thisMonth, todayString);

        for (User user : usersBirthDate) {
            Coupon coupon = new Coupon();

            coupon.setCouponCode(user.getId() + "BD_" + user.getYearOfBirth());
            coupon.setPercentDiscount(false);
            coupon.setDiscountAmount(BigDecimal.valueOf(50000));
            coupon.setMinimumAmount(BigDecimal.valueOf(1));
            coupon.setActive(true);
            coupon.setDescription("Happy Birth Day: " + user.getUsername());
            coupon.setExpired(LocalDateTime.now().plusMonths(1));
            coupon.setUsage(false);
            coupon.setMaxUsage(1);
            coupon.setUsageCount(0);
            coupon.setUsers(new HashSet<>(Set.of(user)));

            couponRepository.save(coupon);

            Notification notification = Notification.builder()
                    .user(user)
                    .book(null)
                    .order(null)
                    .message("Happy Birth Day " + user.getFullName() +
                            ". Hệ thông gửi tặng bạn Coupon nhân dịp sinh nhật bạn." +
                            "Để kiểm tra hãy truy cập: Trang cá nhân -> Coupon")
                    .read(false)
                    .createdAt(new Timestamp(System.currentTimeMillis()))
                    .type(NotificationType.COUPON)
                    .build();

            notificationRepository.save(notification);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CouponDTO> updateCoupon(String code, CouponDTO couponDTO) {
        Coupon coupon = couponRepository.findByCouponCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon Not Found"));

        coupon.setDiscountAmount(couponDTO.getDiscountAmount());
        coupon.setPercentDiscount(couponDTO.isPercentDiscount());
        coupon.setDiscountPercent(couponDTO.getDiscountPercent());
        coupon.setMinimumAmount(couponDTO.getMinimumAmount());
        coupon.setActive(couponDTO.isActive());
        coupon.setDescription(couponDTO.getDescription());
        coupon.setExpired(couponDTO.getExpired());
        coupon.setUsage(couponDTO.isUsage());
        coupon.setMaxUsage(couponDTO.getMaxUsage());
        coupon.setUsageCount(couponDTO.getUsageCount());

        couponRepository.save(coupon);
        CouponDTO updatedCouponDTO = CouponMapper.toCouponDTO(coupon);
        return ApiResponse.success("Coupon Updated", updatedCouponDTO);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> deleteCoupon(String code) {
        Coupon coupon = couponRepository.findByCouponCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon Not Found"));

        coupon.setActive(false);

        couponRepository.save(coupon);

        return ApiResponse.success("Coupon Deleted");
    }
}
