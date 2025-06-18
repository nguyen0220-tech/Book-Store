package catholic.ac.kr.secureuserapp.security.token;

import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.model.entity.VerificationToken;
import catholic.ac.kr.secureuserapp.repository.VerificationTokenRepository;
import catholic.ac.kr.secureuserapp.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TokenService {

    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;

    // Tạo token xác thực qua email
    public String createVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryTime(LocalDateTime.now().plusDays(1));
        verificationTokenRepository.save(verificationToken);

        return token;
    }

    public void sendUnlockEmail(User user) {
        String token = createVerificationToken(user);

        String verifyLink = "http://localhost:8080/auth/verify?token=" + token;

        emailService.sendSimpleMail(
                user.getUsername(),
                "XÁC THỰC EMAIL",
                "Click để kích hoạt tài khoản: "+verifyLink
        );
    }
}
