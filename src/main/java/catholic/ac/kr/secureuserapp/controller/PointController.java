package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.PointDTO;
import catholic.ac.kr.secureuserapp.security.userdetails.MyUserDetails;
import catholic.ac.kr.secureuserapp.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("points")
@RequiredArgsConstructor
public class PointController {
    private final PointService pointService;

    @GetMapping
    public ResponseEntity<ApiResponse<PointDTO>> getPointByUserId(@AuthenticationPrincipal MyUserDetails user) {
        return ResponseEntity.ok(pointService.getPointByUserId(user.getUser().getId()));
    }
}
