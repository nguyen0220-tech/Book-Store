package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.Status.MessageStatus;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.MessageDTO;
import catholic.ac.kr.secureuserapp.security.userdetails.MyUserDetails;
import catholic.ac.kr.secureuserapp.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("message")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<MessageDTO>>> getChatMessages(
            @AuthenticationPrincipal MyUserDetails sender,
            @RequestParam("recipient") String recipient,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(defaultValue = "ONE_WAY") String direction) {
        if ("ONE_WAY".equalsIgnoreCase(direction)) {
            return ResponseEntity.ok(messageService.getOneWayMessage(sender.getUsername(), recipient, page, size));
        } else
            return ResponseEntity.ok(messageService.getTwoWayMessage(sender.getUsername(), recipient, page, size));
    }

    @PostMapping("save")
    public ResponseEntity<ApiResponse<MessageDTO>> saveMessage(
            @AuthenticationPrincipal MyUserDetails sender,
            @RequestBody MessageDTO messageDTO) {
        messageDTO.setSender(sender.getUsername());
        return ResponseEntity.ok(messageService.saveChatMessage(messageDTO));
    }

    @PutMapping("update/{messageId}/status")
    public ResponseEntity<ApiResponse<MessageDTO>> updateMessage(
            @PathVariable("messageId") Long messageId,
            @RequestParam("status") MessageStatus status) {
        return ResponseEntity.ok(messageService.updateMessageStatus(messageId, status));
    }
}
