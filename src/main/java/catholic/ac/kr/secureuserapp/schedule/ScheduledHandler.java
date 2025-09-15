package catholic.ac.kr.secureuserapp.schedule;

import catholic.ac.kr.secureuserapp.model.entity.Coupon;
import catholic.ac.kr.secureuserapp.repository.CouponRepository;
import catholic.ac.kr.secureuserapp.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ScheduledHandler {
    private final CouponRepository couponRepository;
    private final NotificationService notificationService;
    private final CouponService couponService;
    private final RefreshTokenService refreshTokenService;
    private final RankService rankService;
    private final BookService bookService;

    @Scheduled(cron = "0 37 5 * * *")
    public void cleanExpiredCoupons() {
        List<Coupon> expiredCoupons = couponRepository.findAll().stream()
                .filter(coupon -> coupon.getExpired().isBefore(LocalDateTime.now()))
                .toList();

        for (Coupon coupon : expiredCoupons) {
            coupon.setActive(false);
            couponRepository.save(coupon);
        }
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void notificationExpiredCoupons() {
        notificationService.createCouponExpiredNotification();
    }

    @Scheduled(cron ="0 0 3 * * *" )
    public void giveCouponBirthDate(){couponService.giveCouponToUserBirthDate();}

    @Scheduled(cron = "00 53 18 * * *")
    public void clearTokensExpired(){refreshTokenService.clearTokenExpired();}

    @Scheduled(cron = "00 03 5 * * *")
    public void setRankToUse(){rankService.updateRankToUser();}

    @Scheduled(cron = "00 15 022 * * *")
    public void setOriginalPriceBook(){bookService.resetPriceBook();}
}