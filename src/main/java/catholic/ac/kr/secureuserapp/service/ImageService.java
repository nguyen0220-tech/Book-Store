package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.Status.ImageType;
import catholic.ac.kr.secureuserapp.exception.ResourceNotFoundException;
import catholic.ac.kr.secureuserapp.mapper.ImageMapper;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.ImageDTO;
import catholic.ac.kr.secureuserapp.model.entity.Image;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.ImageRepository;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;
import java.util.Optional;

@Service
public class ImageService {
    private final String API_KEY;
    private final String IBB_API;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;

    public ImageService(
            @Value("${ibb.api_key}") String API_KEY,
            @Value("${ibb.api_url}") String IBB_API,
            UserRepository userRepository,
            ImageRepository imageRepository) {

        this.API_KEY = API_KEY;
        this.IBB_API = IBB_API;
        this.userRepository = userRepository;
        this.imageRepository = imageRepository;
    }

    public ApiResponse<String> uploadAvatar(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<Image> isAvatar = imageRepository.findByUserAndSelected(user, true);

        if (isAvatar.isPresent()) {
            Image avatar = isAvatar.get();
            avatar.setSelected(false);
            imageRepository.save(avatar);
        }


        try {
//            1.Đọc file sang mảng byte
            byte[] fileBytes = file.getBytes();

//            2.Chuyển sang Base64 string (ImgBB yêu cầu dữ liệu ảnh phải được gửi dưới dạng Base64.)
            String base64Image = Base64.getEncoder().encodeToString(fileBytes);

//            3.Gửi POST request đến ImgBB
//          WebClient là HTTP client hiện đại của Spring, dùng để gửi HTTP request đến API khác (REST API, cloud, v.v.).
            WebClient webClient = WebClient.create();

            String response = webClient.post() //send: POST
                    .uri(IBB_API + "?key=" + API_KEY) // URL của API ImgBB
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData("image", base64Image)) //hêm dữ liệu “image” (đã Base64) vào body
                    .retrieve() //send request
                    .bodyToMono(String.class) //đọc phản hồi về dạng String
                    .block(); //chờ kết quả (đồng bộ hoá, vì WebClient mặc định là async)

//            4.Parse JSON
            if (response != null && response.contains("\"url\"")) {
                int start = response.indexOf("\"url\":\"") + 7;
                int end = response.indexOf("\"", start);

                Image uploadAvatar = Image.builder()
                        .user(user)
                        .imageUrl(response.substring(start, end))
                        .type(ImageType.AVATAR)
                        .isSelected(true)
                        .build();

                imageRepository.save(uploadAvatar);
                return ApiResponse.success("upload avatar success", response.substring(start, end));
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image" + e.getMessage(), e);
        }
    }

    public ApiResponse<Page<ImageDTO>> getAvatar(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("uploadAt").descending());

        Page<Image> imagePage = imageRepository.findByUserAndType(user, ImageType.AVATAR, pageable);

        Page<ImageDTO> imageDTOS = imagePage.map(ImageMapper::convertToDTO);

        return ApiResponse.success("get avatar success", imageDTOS);
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
}
