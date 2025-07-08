package catholic.ac.kr.secureuserapp.schedule;

import catholic.ac.kr.secureuserapp.model.entity.Coupon;
import catholic.ac.kr.secureuserapp.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CouponCleanupTask {
    private final CouponRepository couponRepository;

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanExpiredCoupons() {
        List<Coupon> expiredCoupons = couponRepository.findAll().stream()
                .filter(coupon -> coupon.getExpired().isBefore(LocalDateTime.now()))
                .toList();

        couponRepository.deleteAll(expiredCoupons);
    }
}
