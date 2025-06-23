package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.RefreshTokenDTO;
import catholic.ac.kr.secureuserapp.security.userdetails.MyUserDetails;
import catholic.ac.kr.secureuserapp.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("devices")
@RequiredArgsConstructor
public class DeviceTokenController {
    private final RefreshTokenService refreshTokenService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RefreshTokenDTO>>> getAllDerives(@AuthenticationPrincipal MyUserDetails userDetails) {
        List<RefreshTokenDTO> tokens = refreshTokenService.getAllByUser(userDetails.getUser()).getData();
        return ResponseEntity.ok(ApiResponse.success("tokens", tokens));
    }

    @DeleteMapping("/{deviceId}")
    public ResponseEntity<ApiResponse<?>> logoutFromDevice(@PathVariable String deviceId, @AuthenticationPrincipal MyUserDetails userDetails) {
        boolean result = refreshTokenService.revokeByDeviceId(userDetails.getUser(), deviceId);
        if (result) {
            return ResponseEntity.ok(ApiResponse.success("Logged out successfully from device " + deviceId));
        } else {
            return ResponseEntity.ok(ApiResponse.error("Derive not found "));
        }
    }
}
