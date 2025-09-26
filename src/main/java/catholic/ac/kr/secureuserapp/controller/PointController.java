package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.PointDTO;
import catholic.ac.kr.secureuserapp.model.dto.PointHistory;
import catholic.ac.kr.secureuserapp.model.dto.request.PointRequest;
import catholic.ac.kr.secureuserapp.security.userdetails.MyUserDetails;
import catholic.ac.kr.secureuserapp.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("points")
@RequiredArgsConstructor
public class PointController {
    private final PointService pointService;

    @GetMapping("all")
    public ResponseEntity<ApiResponse<Page<PointDTO>>> getAllPoints(
            @RequestParam int page,
            @RequestParam int size) {
        return ResponseEntity.ok(pointService.getAllPoints(page, size));
    }

    @PostMapping("by-username")
    public ResponseEntity<ApiResponse<PointDTO>> getPointByUsername(@RequestBody PointRequest request) {
        return ResponseEntity.ok(pointService.getPointByUsername(request.getUsername()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PointDTO>> getPointByUserId(@AuthenticationPrincipal MyUserDetails user) {
        return ResponseEntity.ok(pointService.getPointByUserId(user.getUser().getId()));
    }

    @GetMapping("history")
    public ResponseEntity<ApiResponse<Page<PointHistory>>> getHistoryPoints(
            @AuthenticationPrincipal MyUserDetails user,
            @RequestParam int page,
            @RequestParam int size) {
        return ResponseEntity.ok(pointService.getPointHistoryByUserId(user.getUser().getId(), page, size));
    }
}
