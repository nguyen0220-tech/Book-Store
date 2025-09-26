package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.model.dto.*;
import catholic.ac.kr.secureuserapp.security.userdetails.MyUserDetails;
import catholic.ac.kr.secureuserapp.service.ChatRoomService;
import catholic.ac.kr.secureuserapp.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("chat-room")
@RequiredArgsConstructor
public class ChatRoomController {
    private final ChatRoomService chatRoomService;
    private final MessageService messageService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ChatRoomDTO>>> getChatRooms(
            @AuthenticationPrincipal MyUserDetails user,
            @RequestParam int page,
            @RequestParam int size) {
        return ResponseEntity.ok(chatRoomService.getChatRoomsByUserId(user.getUser().getId(), page, size));
    }

    @PostMapping("{chatRoomId}")
    public ResponseEntity<ApiResponse<Page<MessageForGroupChatDTO>>> getChatMessagesForChatRoom(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @PathVariable Long chatRoomId,
            @RequestParam int page,
            @RequestParam int size) {
        return ResponseEntity.ok(messageService.getMessagesFromChatRoom(userDetails.getUser().getId(), chatRoomId, page, size));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> createChatRoom(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @RequestBody ChatRoomRequest request
    ) {
        return ResponseEntity.ok(chatRoomService.createChatRoom(userDetails.getUser().getId(), request));
    }

    @PostMapping("act-member")
    public ResponseEntity<ApiResponse<String>> actMemberToChatRoom(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @RequestBody ActMemberToGroupRequest request){
        return ResponseEntity.ok(chatRoomService.setMemberToChatRoom(
                userDetails.getUser().getId(),
                request.getChatRoomId(),
                request.getMemberId(),
                request.getAct()));
    }

}
