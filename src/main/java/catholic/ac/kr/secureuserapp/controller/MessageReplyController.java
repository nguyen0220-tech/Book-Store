package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.MessageReplyDTO;
import catholic.ac.kr.secureuserapp.model.dto.request.MessageReplyRequest;
import catholic.ac.kr.secureuserapp.security.userdetails.MyUserDetails;
import catholic.ac.kr.secureuserapp.service.MessageReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("reply-message")
@RequiredArgsConstructor
public class MessageReplyController {
    private final MessageReplyService messageReplyService;

    @PostMapping
    public ResponseEntity<ApiResponse<MessageReplyDTO>> createMessageReply(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @RequestBody MessageReplyRequest request){
        return ResponseEntity.ok(messageReplyService.createMessageReply(
                userDetails.getUser().getId(),
                request.getMessageId(),
                request.getReplyText()));
    }
}
