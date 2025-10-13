package catholic.ac.kr.secureuserapp.uploadhandler;

import catholic.ac.kr.secureuserapp.Status.ImageType;
import catholic.ac.kr.secureuserapp.exception.ResourceNotFoundException;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.ImageRepository;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class UploadFileHandler {
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final Cloudinary cloudinary;

    public String uploadFile(Long userId, MultipartFile file) {
        try {
            Map result = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "media_" + userId,
                            "public_id", "user_id" + userId + "_" + LocalDateTime.now(),
                            "overwrite", true));

            return result.get("url").toString();
        } catch (Exception e) {
            throw new RuntimeException("file upload error",e);
        }
    }
}
