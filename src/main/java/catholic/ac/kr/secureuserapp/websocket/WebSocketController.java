package catholic.ac.kr.secureuserapp.websocket;

import catholic.ac.kr.secureuserapp.model.dto.MessageDTO;
import catholic.ac.kr.secureuserapp.service.MessageService;
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

    @MessageMapping("chat")
    public void handleChatMessage(@Payload MessageDTO messageDTO, Principal principal) {
        if (principal == null) {
            System.out.println("❌ Principal is null");
            return;
        }

        String username = principal.getName();
        System.out.println("Sender: " + username);
        System.out.println("Recipient: " + messageDTO.getRecipient());
        System.out.println("Message: " + messageDTO.getMessage());

        messageDTO.setSender(username);

        MessageDTO savedMessage = messageService.saveChatMessage(messageDTO).getData();

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
