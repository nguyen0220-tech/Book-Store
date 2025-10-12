package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.ImageDTO;
import catholic.ac.kr.secureuserapp.security.userdetails.MyUserDetails;
import catholic.ac.kr.secureuserapp.service.ImageService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("upload")
@RequiredArgsConstructor
public class ImageController {
    private final ImageService imageService;

    @PostMapping("/avatar")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(imageService.uploadAvatar(userDetails.getUser().getId(), file));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ImageDTO>>> getAvatar(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @RequestParam int page,
            @RequestParam int size) {
        return ResponseEntity.ok(imageService.getAvatar(userDetails.getUser().getId(), page, size));
    }

    @PutMapping("/avatar/change")
    public ResponseEntity<ApiResponse<String>> changeAvatar(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @RequestParam Long imageId){
        return ResponseEntity.ok(imageService.changeAvatar(userDetails.getUser().getId(), imageId));
    }
}
