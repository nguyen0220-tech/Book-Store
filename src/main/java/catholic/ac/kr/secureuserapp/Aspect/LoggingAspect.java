package catholic.ac.kr.secureuserapp.Aspect;

import catholic.ac.kr.secureuserapp.model.dto.SignupRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j // Tự tạo log: Logger log = LoggerFactory.getLogger(TenClass)
@Aspect// Đánh dấu đây là 1 Aspect để AOP hoạt động
@Component // Cho phép Spring quản lý class này như một bean
public class LoggingAspect {

    // Advice này sẽ được chạy "xung quanh" tất cả các method trong gói service
    // Expression AOP để chỉ định các method cần áp dụng: * là tất cả kiểu trả về, (..) là mọi tham số
    @Around("execution(* catholic.ac.kr.secureuserapp.service.*.*(..)) ||" +
            "execution(* catholic.ac.kr.secureuserapp.controller.*.*(..))")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toLongString();
        log.info("==> Bắt đầu gọi: {}", methodName);

        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof SignupRequest signupRequest) {
                log.info("==> SignupRequest: {}", signupRequest.getUsername());
            }
        }

        long startTime = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long duration = System.currentTimeMillis() - startTime;
        log.info("<== Kết thúc: {} | Thời gian: {}ms", methodName, duration);

        return result;
    }
}
