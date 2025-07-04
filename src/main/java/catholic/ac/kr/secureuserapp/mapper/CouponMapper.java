package catholic.ac.kr.secureuserapp.mapper;

import catholic.ac.kr.secureuserapp.model.dto.CouponDTO;
import catholic.ac.kr.secureuserapp.model.dto.CouponRequest;
import catholic.ac.kr.secureuserapp.model.entity.Coupon;

import java.util.ArrayList;
import java.util.List;

public class CouponMapper {
    public static CouponDTO toCouponDTO(Coupon coupon) {
        CouponDTO couponDTO = new CouponDTO();

        couponDTO.setCouponCode(coupon.getCouponCode());
        couponDTO.setDiscountAmount(coupon.getDiscountAmount());
        couponDTO.setPercentDiscount(coupon.isPercentDiscount());
        couponDTO.setDiscountPercent(coupon.getDiscountPercent());
        couponDTO.setMinimumAmount(coupon.getMinimumAmount());
        couponDTO.setActive(coupon.isActive());
        couponDTO.setDescription(coupon.getDescription());
        couponDTO.setExpired(coupon.getExpired());
        couponDTO.setUsage(coupon.isUsage());

        return couponDTO;
    }

    public static List<CouponDTO> toCouponDTO(List<Coupon> coupons) {
        if (coupons == null) {
            return null;
        }

        List<CouponDTO> couponDTOs = new ArrayList<>();
        for (Coupon coupon : coupons) {
            couponDTOs.add(toCouponDTO(coupon));
        }
        return couponDTOs;
    }

    public static Coupon toCoupon(CouponRequest request) {
        Coupon coupon = new Coupon();
        coupon.setCouponCode(request.getCouponCode());
        coupon.setDiscountAmount(request.getDiscountAmount());
        coupon.setPercentDiscount(request.isPercentDiscount());
        coupon.setDiscountPercent(request.getDiscountPercent());
        coupon.setActive(request.isActive());
        coupon.setMinimumAmount(request.getMinimumAmount());
        coupon.setDescription(request.getDescription());
        coupon.setExpired(request.getExpired());
        coupon.setUsage(request.isUsage());
        coupon.setMaxUsage(request.getMaxUsage());
        coupon.setUsageCount(request.getUsageCount());

        return coupon;
    }
}
