package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.Status.NotificationType;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.NotificationDTO;
import catholic.ac.kr.secureuserapp.model.dto.NotificationMessageRequest;
import catholic.ac.kr.secureuserapp.security.userdetails.MyUserDetails;
import catholic.ac.kr.secureuserapp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("notify")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationDTO>>> getNotifications(
            @AuthenticationPrincipal MyUserDetails user,
            @RequestParam int page,
            @RequestParam int size) {
        return ResponseEntity.ok(notificationService.getAllNotifications(user.getUser().getId(), page, size));
    }

    @GetMapping("{notificationId}")
    public ResponseEntity<ApiResponse<NotificationDTO>> getNotification(
            @AuthenticationPrincipal MyUserDetails user,
            @PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.getNotification(user.getUser().getId(), notificationId));
    }

    @GetMapping("filter")
    public ResponseEntity<ApiResponse<Page<NotificationDTO>>> getNotificationsAndFilter(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @RequestParam NotificationType type,
            @RequestParam int page,
            @RequestParam int size){
        return ResponseEntity.ok(notificationService.getNotificationByUserIdAnFilterType(userDetails.getUser().getId(), type,page,size));
    }

    @PutMapping("{notificationId}/read")
    public ResponseEntity<ApiResponse<String>> markNotificationAsRead(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.markNotificationAsRead(userDetails.getUser().getId(), notificationId));
    }

    @GetMapping("un-read")
    public ResponseEntity<ApiResponse<Integer>> countUnreadNotifications(@AuthenticationPrincipal MyUserDetails userDetails) {
        return ResponseEntity.ok(notificationService.countNotificationUnread(userDetails.getUser().getId()));
    }

    @PostMapping("creat")
    public ResponseEntity<ApiResponse<Map<String,Object>>> createNotification(@RequestBody NotificationMessageRequest message){
        return ResponseEntity.ok(notificationService.createSystemNotification(message.getMessage()));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<ApiResponse<String>> deleteNotification(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.deleteNotification(id));
    }
}
