package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.exception.ResourceNotFoundException;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.entity.Coupon;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.CouponRepository;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CouponClaimService {
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ApiResponse<String> claimCoupon(Long userId,String couponCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->new ResourceNotFoundException("User not found"));

        Coupon coupon = couponRepository.findByCouponCode(couponCode)
                .orElseThrow(() ->new ResourceNotFoundException("Coupon not found"));

        if (!coupon.isActive() || coupon.getExpired().isBefore(LocalDateTime.now())){
            return ApiResponse.error("Coupon expired");
        }

        else if (coupon.getUsers().contains(user)){
            return ApiResponse.error("Coupon already claimed");
        }

        else if (coupon.getUsageCount() >= coupon.getMaxUsage()){
            return ApiResponse.error("Coupon exceeded max usage");
        }

        coupon.getUsers().add(user);
        couponRepository.save(coupon);

        return ApiResponse.success("Coupon Claimed successfully");
    }
}
