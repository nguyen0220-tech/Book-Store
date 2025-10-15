package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.Status.ImageType;
import catholic.ac.kr.secureuserapp.exception.ResourceNotFoundException;
import catholic.ac.kr.secureuserapp.mapper.ImageMapper;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.ImageDTO;
import catholic.ac.kr.secureuserapp.model.dto.UserAvatarDTO;
import catholic.ac.kr.secureuserapp.model.entity.Image;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.ImageRepository;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import catholic.ac.kr.secureuserapp.uploadhandler.UploadFileHandler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageService {
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final UploadFileHandler uploadFileHandler;

    public ImageService(
            UserRepository userRepository,
            ImageRepository imageRepository,
            UploadFileHandler uploadFileHandler) {

        this.userRepository = userRepository;
        this.imageRepository = imageRepository;
        this.uploadFileHandler = uploadFileHandler;
    }

    public ApiResponse<String> uploadAvatar(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        imageRepository.findByUserAndSelected(user, true)
                .ifPresent(avatar -> {
                    avatar.setSelected(false);
                    imageRepository.save(avatar);
                });

        if(file.isEmpty())

    {
        return ApiResponse.error("File is empty");
    }

    String imageUrl = uploadFileHandler.uploadFile(userId, file);

    Image newAvatar = Image.builder()
            .user(user)
            .imageUrl(imageUrl)
            .type(ImageType.AVATAR)
            .isSelected(true)
            .build();

            imageRepository.save(newAvatar);

        return ApiResponse.success("Image uploaded successfully",imageUrl);
}

public ApiResponse<Page<ImageDTO>> getAvatars(Long userId, int page, int size) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    Pageable pageable = PageRequest.of(page, size, Sort.by("uploadAt").descending());

    Page<Image> imagePage = imageRepository.findByUserAndType(user, ImageType.AVATAR, pageable);

    Page<ImageDTO> imageDTOS = imagePage.map(ImageMapper::convertToDTO);

    return ApiResponse.success("get avatar success", imageDTOS);
}

public ApiResponse<UserAvatarDTO> getUserAvatar(Long userId) {

        UserAvatarDTO avatarDTO = imageRepository.findAvatarUrlByUserId(userId);

        return ApiResponse.success("get avatar url success", avatarDTO);
}

public ApiResponse<String> changeAvatar(Long userId, Long imageId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    Image avatar = imageRepository.findByUserAndSelected(user, true)
            .orElseThrow(() -> new ResourceNotFoundException("Image not found"));

    avatar.setSelected(false);

    Image image = imageRepository.findByUserAndId(user, imageId)
            .orElseThrow(() -> new ResourceNotFoundException("image not found"));

    image.setSelected(true);

    imageRepository.save(image);

    return ApiResponse.success("changed avatar success", image.getImageUrl());
}

public ApiResponse<String> deleteAvatar(Long userId, Long imageId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    Image image = imageRepository.findByUserAndId(user, imageId)
            .orElseThrow(() -> new ResourceNotFoundException("Image not found"));

    imageRepository.delete(image);

    return ApiResponse.success("deleted avatar success");
}
}
