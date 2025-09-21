package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.model.dto.*;
import catholic.ac.kr.secureuserapp.security.userdetails.MyUserDetails;
import catholic.ac.kr.secureuserapp.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("friend")
@RequiredArgsConstructor
public class FriendController {
    private final FriendService friendService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<FriendDTO>>> getFriends(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @RequestParam int page,
            @RequestParam int size) {
        return ResponseEntity.ok(friendService.getFriends(userDetails.getUser().getId(), page, size));
    }

    @GetMapping("pending")
    public ResponseEntity<ApiResponse<Page<FriendDTO>>> getPendingFriend(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @RequestParam int page,
            @RequestParam int size) {
        return ResponseEntity.ok(friendService.getPendingFriendRequests(userDetails.getUser().getId(), page, size));
    }

    @GetMapping("blocking")
    public ResponseEntity<ApiResponse<Page<FriendDTO>>> getBlockingFriend(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @RequestParam int page,
            @RequestParam int size) {
        return ResponseEntity.ok(friendService.getBlockingFriend(userDetails.getUser().getId(), page, size));
    }

    @GetMapping("to-give")
    public ResponseEntity<ApiResponse<List<ToGiveFriendDTO>>> getToGiveFriend(@AuthenticationPrincipal MyUserDetails userDetails){
        return ResponseEntity.ok(friendService.getToGiveFriends(userDetails.getUser().getId()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FriendDTO>> addFriend(@AuthenticationPrincipal MyUserDetails userDetails, @RequestBody FriendRequest request) {
        return ResponseEntity.ok(friendService.addFriend(userDetails.getUser().getId(), request.getFriendId()));
    }

    @PutMapping("accept")
    public ResponseEntity<ApiResponse<String>> acceptFriend(@AuthenticationPrincipal MyUserDetails userDetails, @RequestBody FriendRequest request) {
        return ResponseEntity.ok(friendService.acceptFriendRequest(userDetails.getUser().getId(), request.getFriendId()));
    }

    @PutMapping("cancel")
    public ResponseEntity<ApiResponse<String>> rejectFriend(@AuthenticationPrincipal MyUserDetails userDetails, @RequestBody FriendRequest request) {
        return ResponseEntity.ok(friendService.cancelFriendRequest(userDetails.getUser().getId(), request.getFriendId()));
    }

    @DeleteMapping("{friendId}")
    public ResponseEntity<ApiResponse<String>> deleteFriend(@AuthenticationPrincipal MyUserDetails userDetails, @PathVariable Long friendId) {
        return ResponseEntity.ok(friendService.deleteFriend(userDetails.getUser().getId(), friendId));
    }

    @PutMapping("block")
    public ResponseEntity<ApiResponse<String>> blockFriend(@AuthenticationPrincipal MyUserDetails userDetails, @RequestBody FriendRequest request) {
        return ResponseEntity.ok(friendService.blockFriend(userDetails.getUser().getId(), request.getFriendId()));
    }

    @PutMapping("un-block")
    public ResponseEntity<ApiResponse<String>> unBlockFriend(@AuthenticationPrincipal MyUserDetails userDetails, @RequestBody FriendRequest request) {
        return ResponseEntity.ok(friendService.unBlockFriend(userDetails.getUser().getId(), request.getFriendId()));
    }

    @GetMapping("count")
    public ResponseEntity<ApiResponse<Integer>> countFriends(@AuthenticationPrincipal MyUserDetails userDetails) {
        return ResponseEntity.ok(friendService.countFriends(userDetails.getUser().getId()));
    }

    @GetMapping("count-pending")
    public ResponseEntity<ApiResponse<Integer>> getRequestFriends(@AuthenticationPrincipal MyUserDetails userDetails) {
        return ResponseEntity.ok(friendService.countRequestFriends(userDetails.getUser().getId()));
    }

    @GetMapping("send-request")
    public ResponseEntity<ApiResponse<Page<FriendDTO>>> getSendRequestFriends(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @RequestParam int page,
            @RequestParam int size) {
        return ResponseEntity.ok(friendService.getSendRequestFriend(userDetails.getUser().getId(), page, size));
    }

    @PutMapping("cancel-request")
    public ResponseEntity<ApiResponse<String>> cancelRequestFriend(@AuthenticationPrincipal MyUserDetails userDetails, @RequestBody FriendRequest request) {
        return ResponseEntity.ok(friendService.cancelFriendRequest(userDetails.getUser().getId(), request.getFriendId()));
    }

    @GetMapping("with-admin")
    public ResponseEntity<ApiResponse<Page<FriendChatDTO>>> getFriendsAndAdminToChat(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @RequestParam int page,
            @RequestParam int size){
        return ResponseEntity.ok(friendService.getAllFriendAndAdminToChatMessage(userDetails.getUser().getId(), page, size));
    }
}
