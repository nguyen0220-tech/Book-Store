package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.Status.MessageStatus;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.ChatMessageDTO;
import catholic.ac.kr.secureuserapp.security.userdetails.MyUserDetails;
import catholic.ac.kr.secureuserapp.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("message")
@RequiredArgsConstructor
public class ChatMessageController {
    private final ChatMessageService chatMessageService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ChatMessageDTO>>> getChatMessages(
            @AuthenticationPrincipal MyUserDetails sender,
            @RequestParam("recipient") String recipient,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(defaultValue = "ONE_WAY") String direction) {
        if ("ONE_WAY".equalsIgnoreCase(direction)) {
            return ResponseEntity.ok(chatMessageService.getOneWayMessage(sender.getUsername(), recipient, page, size));
        } else
            return ResponseEntity.ok(chatMessageService.getTwoWayMessage(sender.getUsername(), recipient, page, size));
    }

    @PostMapping("save")
    public ResponseEntity<ApiResponse<ChatMessageDTO>> saveMessage(
            @AuthenticationPrincipal MyUserDetails sender,
            @RequestBody ChatMessageDTO chatMessageDTO) {
        chatMessageDTO.setSender(sender.getUsername());
        return ResponseEntity.ok(chatMessageService.saveChatMessage(chatMessageDTO));
    }

    @PutMapping("update/{messageId}/status")
    public ResponseEntity<ApiResponse<ChatMessageDTO>> updateMessage(
            @PathVariable("messageId") Long messageId,
            @RequestParam("status") MessageStatus status) {
        return ResponseEntity.ok(chatMessageService.updateMessageStatus(messageId, status));
    }
}
