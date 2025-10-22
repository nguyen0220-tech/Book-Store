package catholic.ac.kr.secureuserapp.common;

import catholic.ac.kr.secureuserapp.model.entity.Image;
import catholic.ac.kr.secureuserapp.repository.ImageRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component

public class CommonService {
    public final ImageRepository imageRepository;
    public final PasswordEncoder passwordEncoder;

    public CommonService(ImageRepository imageRepository, PasswordEncoder passwordEncoder) {
        this.imageRepository = imageRepository;
        this.passwordEncoder = passwordEncoder;

    }

    public Map<Long, String> getUserIdAvatarUrl(Set<Long> userIds) {
        Map<Long, String> res = new ConcurrentHashMap<>();

        for (Long userId : userIds) {
            String avatarUrl = imageRepository.findByUserId(userId)
                    .map(Image::getImageUrl)
                    .orElse("/icon/default-avatar.png"); // ảnh mặc định
            res.put(userId, avatarUrl);
        }
        return res;
    }

    public boolean checkInputPassword(String oldPassword, String newPassword) {
        return !(passwordEncoder.matches(oldPassword, newPassword));
    }

}
