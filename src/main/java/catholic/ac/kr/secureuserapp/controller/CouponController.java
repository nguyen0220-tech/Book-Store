package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.request.CouponClaimRequest;
import catholic.ac.kr.secureuserapp.model.dto.CouponDTO;
import catholic.ac.kr.secureuserapp.model.dto.request.CouponRequest;
import catholic.ac.kr.secureuserapp.security.userdetails.MyUserDetails;
import catholic.ac.kr.secureuserapp.service.CouponClaimService;
import catholic.ac.kr.secureuserapp.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("coupon")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;
    private final CouponClaimService couponClaimService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CouponDTO>>> getCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<CouponDTO>> getCouponByCode(@PathVariable String code) {
        return ResponseEntity.ok(couponService.getCouponByCode(code));
    }

    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<CouponDTO>>> getCouponsByUserId(@AuthenticationPrincipal MyUserDetails user) {
        return ResponseEntity.ok(couponService.getCouponByUserId(user.getUser().getId()));
    }

    @GetMapping("filter")
    public ResponseEntity<ApiResponse<List<CouponDTO>>> getAndFilterCoupons(@RequestParam boolean active){
        return ResponseEntity.ok(couponService.getAndFilterCoupons(active));
    }

    @PostMapping("add")
    public ResponseEntity<ApiResponse<CouponDTO>> createCoupon(@RequestBody CouponRequest request) {
        return ResponseEntity.ok(couponService.createCoupon(request));
    }

    @PutMapping("{code}")
    public ResponseEntity<ApiResponse<CouponDTO>> updateCoupon(@PathVariable String code, @RequestBody CouponRequest request) {
        return ResponseEntity.ok(couponService.updateCoupon(code, request));
    }

    @DeleteMapping("{code}")
    public ResponseEntity<ApiResponse<String>> deleteCoupon(@PathVariable String code) {
        return ResponseEntity.ok(couponService.deleteCoupon(code));
    }

    @PostMapping("claim")
    public ResponseEntity<ApiResponse<String>> claimCoupon(
            @RequestBody CouponClaimRequest request,
            @AuthenticationPrincipal MyUserDetails currentUser) {
        Long userId = currentUser.getUser().getId();
        return ResponseEntity.ok(couponClaimService.claimCoupon(userId, request.getCouponCode()));
    }
}
