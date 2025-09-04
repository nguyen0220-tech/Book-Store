//package catholic.ac.kr.secureuserapp.websocket;
//
//import catholic.ac.kr.secureuserapp.model.dto.ChatMessageDTO;
//import catholic.ac.kr.secureuserapp.service.ChatMessageService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.Payload;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Controller;
//
//import java.security.Principal;
//
//@Controller
//@RequiredArgsConstructor
//public class WebSocketController {
//    private final SimpMessagingTemplate messagingTemplate; //gửi message đến client qua WebSocket
//    private final ChatMessageService chatMessageService;
//
//    @MessageMapping("chat") //Lắng nghe tin nhắn từ client gửi tới /app/chat
//    public void handleChatMessage(@Payload ChatMessageDTO messageDTO, Principal principal) {
//        System.out.println("Sender: " + messageDTO.getSender());
//        System.out.println("Recipient: " + messageDTO.getRecipient());
//        System.out.println("Principal: " + principal.getName());
//
//        if (principal == null) {
//            System.out.println("Principal is null!");
//            return;
//        }
//
//        System.out.println("WebSocket nhận message: " + messageDTO.getMessage());
//
//        String username = principal.getName();
//        messageDTO.setSender(username);
//
//        ChatMessageDTO saveMessage = chatMessageService.saveChatMessage(messageDTO).getData();
//
//        messagingTemplate.convertAndSendToUser(
//                saveMessage.getRecipient(),
//                "/topic/message", //client phải subscribe endpoint này
//                saveMessage
//        );
//
//
//    }
//}
//
//    /*
//    | Thành phần                                                 | Mục đích                                           |
//| ---------------------------------------------------------- | -------------------------------------------------- |
//| `@MessageMapping`                                          | Nhận tin nhắn từ client gửi tới WebSocket endpoint |
//| `@SendToUser` / `messagingTemplate.convertAndSendToUser()` | Gửi tin nhắn đến người nhận                        |
//| `ChatMessageDTO`                                           | Dữ liệu tin nhắn giữa client và server             |
//| `ChatMessageService`                                       | Lưu DB và xử lý logic                              |
//
//     */
//
// /*
// Client gửi JSON đến WebSocket endpoint:
//ws://<server>/ws và gửi message đến /app/chat
//Ví dụ:
//{
//  "sender": "customerA",
//  "recipient": "admin",
//  "content": "Tôi cần hỗ trợ",
//  "timestamp": "...",
//  "status": "SENT"
//}
//
//Server nhận được tại:
//@MessageMapping("/chat")
//public void handleChatMessage(ChatMessageDTO messageDTO)
//
//Tin nhắn được: Lưu vào database qua:
//ChatMessageDTO saved = chatMessageService.saveChatMessage(messageDTO).getData();
//
//Gửi lại cho người nhận:
//messagingTemplate.convertAndSendToUser(saved.getRecipient(), "/queue/messages", saved);
//  */
