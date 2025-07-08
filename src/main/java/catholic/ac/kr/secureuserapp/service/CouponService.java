package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.exception.ResourceNotFoundException;
import catholic.ac.kr.secureuserapp.mapper.CouponMapper;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.CouponDTO;
import catholic.ac.kr.secureuserapp.model.dto.CouponRequest;
import catholic.ac.kr.secureuserapp.model.entity.Coupon;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.CouponRepository;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<CouponDTO>> getAllCoupons() {
        List<Coupon> coupons = couponRepository.findAll();
        //delete valid coupon with stream
        coupons.removeIf(coupon -> {
            boolean isExpired = coupon.getExpired().isAfter(LocalDateTime.now());
            if (isExpired) {
                couponRepository.delete(coupon);
            }
            return isExpired;
        });
        List<CouponDTO> couponDTOS = coupons.stream()
                .map(CouponMapper::toCouponDTO)
                .toList();

        return ApiResponse.success("All coupon", couponDTOS);

    }

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CouponDTO> getCouponByCode(String code) {
        Coupon coupon = couponRepository.findByCouponCode(code)
                .orElseThrow(()-> new ResourceNotFoundException("Coupon Not Found"));

        CouponDTO couponDTO = CouponMapper.toCouponDTO(coupon);

        return ApiResponse.success("Coupon found", couponDTO);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ApiResponse<List<CouponDTO>> getCouponByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User Not Found"));

        List<Coupon> coupons = couponRepository.findByUserId(user.getId());

        List<Coupon> validCoupons = new ArrayList<>();
        for (Coupon coupon : coupons) {
            if (coupon.getExpired().isBefore(LocalDateTime.now())){
                couponRepository.delete(coupon);
            }
            else {
                validCoupons.add(coupon);
            }
        }

        List<CouponDTO> couponDTOS = CouponMapper.toCouponDTO(validCoupons);

        return ApiResponse.success("Coupon found with userid "+userId,couponDTOS);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CouponDTO> createCoupon(CouponRequest request) {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        RandomGenerator randomGenerator = RandomGenerator.getDefault();

        String randomCode = randomGenerator.ints(10,0,chars.length())
                .mapToObj( i -> String.valueOf(chars.charAt(i)))
                .collect( Collectors.joining() );
        request.setCouponCode(randomCode.toUpperCase());

        Coupon coupon = CouponMapper.toCoupon(request);

        couponRepository.save(coupon);

        CouponDTO couponDTO = CouponMapper.toCouponDTO(coupon);

        return ApiResponse.success("Coupon Created", couponDTO);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CouponDTO> updateCoupon(String code,CouponDTO couponDTO) {
        Coupon coupon = couponRepository.findByCouponCode(code)
                .orElseThrow(()-> new ResourceNotFoundException("Coupon Not Found"));

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
                .orElseThrow(()-> new ResourceNotFoundException("Coupon Not Found"));

        couponRepository.delete(coupon);

        return ApiResponse.success("Coupon Deleted");
    }


}
