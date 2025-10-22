package catholic.ac.kr.secureuserapp.websocket;

import catholic.ac.kr.secureuserapp.model.dto.*;
import catholic.ac.kr.secureuserapp.model.dto.request.MessageForChatRoomRequest;
import catholic.ac.kr.secureuserapp.model.dto.request.MessageReplyRequest;
import catholic.ac.kr.secureuserapp.model.dto.request.MessageRequest;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import catholic.ac.kr.secureuserapp.service.MessageService;
import catholic.ac.kr.secureuserapp.service.MessageReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class WebSocketController {
    private final SimpMessagingTemplate messagingTemplate; //gửi message đến client qua WebSocket
    private final MessageService messageService;
    private final MessageReplyService messageReplyService;
    private final UserRepository userRepository;

    @MessageMapping("chat")
    public void handleChatMessage(@Payload MessageRequest request, Principal principal) {
        if (principal == null) {
            System.out.println("❌ Principal is null");
            return;
        }

        String username = principal.getName();
        System.out.println("Sender: " + username);
        System.out.println("Recipient: " + request.getRecipient());
        System.out.println("Message: " + request.getMessage());

        request.setSender(username);

        MessageDTO savedMessage = messageService.saveChatMessage(request).getData();

        messagingTemplate.convertAndSendToUser(
                savedMessage.getRecipient(),
                "/queue/message",
                savedMessage
        );

        // Gửi lại cho chính sender để hiển thị ngay
        messagingTemplate.convertAndSendToUser(
                savedMessage.getSender(),
                "/queue/message",
                savedMessage
        );
    }

    @MessageMapping("chat-group")
    public void handleChatRoomMessage(@Payload MessageForChatRoomRequest request, Principal principal) {
        if (principal == null) {
            System.out.println("Principal is null");
            return;
        }
        request.setSender(principal.getName());

        MessageForGroupChatDTO saveMessageForGroup = messageService.saveMessageForChatGroup(request).getData();

        messagingTemplate.convertAndSend(
                "/topic/message" + request.getChatRoomId(),
                saveMessageForGroup);
    }

    @MessageMapping("message-reply")
    public void handleMessageReply(@Payload MessageReplyRequest request, Principal principal) {
        if (principal == null) {
            System.out.println("Principal is null");
            return;
        }

        String username = principal.getName();
        request.setSender(username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        MessageReplyDTO messageReplyDTO = messageReplyService.createMessageReply(user.getId(), request.getMessageId(), request.getReplyText()).getData();

        messagingTemplate.convertAndSend(
                "/topic/message" + request.getChatRoomId(),
                messageReplyDTO
        );
    }
}

    /*
    | Thành phần                                                 | Mục đích                                           |
| ---------------------------------------------------------- | -------------------------------------------------- |
| `@MessageMapping`                                          | Nhận tin nhắn từ client gửi tới WebSocket endpoint |
| `@SendToUser` / `messagingTemplate.convertAndSendToUser()` | Gửi tin nhắn đến người nhận                        |
| `ChatMessageDTO`                                           | Dữ liệu tin nhắn giữa client và server             |
| `ChatMessageService`                                       | Lưu DB và xử lý logic                              |

     */

 /*
 Client gửi JSON đến WebSocket endpoint:
ws://<server>/ws và gửi message đến /app/chat
Ví dụ:
{
  "sender": "customerA",
  "recipient": "admin",
  "content": "Tôi cần hỗ trợ",
  "timestamp": "...",
  "status": "SENT"
}

Server nhận được tại:
@MessageMapping("/chat")
public void handleChatMessage(ChatMessageDTO messageDTO)

Tin nhắn được: Lưu vào database qua:
ChatMessageDTO saved = chatMessageService.saveChatMessage(messageDTO).getData();

Gửi lại cho người nhận:
messagingTemplate.convertAndSendToUser(saved.getRecipient(), "/queue/messages", saved);
  */
