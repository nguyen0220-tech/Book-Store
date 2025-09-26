package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.Status.ChatRoomType;
import catholic.ac.kr.secureuserapp.Status.MessageStatus;
import catholic.ac.kr.secureuserapp.exception.ResourceNotFoundException;
import catholic.ac.kr.secureuserapp.mapper.MessageMapper;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.request.MessageForChatRoomRequest;
import catholic.ac.kr.secureuserapp.model.dto.MessageDTO;
import catholic.ac.kr.secureuserapp.model.dto.MessageForGroupChatDTO;
import catholic.ac.kr.secureuserapp.model.entity.ChatRoom;
import catholic.ac.kr.secureuserapp.model.entity.Message;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.ChatRoomRepository;
import catholic.ac.kr.secureuserapp.repository.MessageRepository;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;

    public ApiResponse<Page<MessageDTO>> getOneWayMessage(String sender, String recipient, int pae, int size) {
        Pageable pageable = PageRequest.of(pae, size, Sort.by("timestamp").descending());
        Page<Message> messages = messageRepository.findBySenderUsernameAndRecipientUsername(sender, recipient, pageable);

        Page<MessageDTO> messageDTOS = messages.map(MessageMapper::toChatMessageDTO);

        return ApiResponse.success("List of chat messages one way", messageDTOS);
    }

    @Transactional
    public ApiResponse<Page<MessageDTO>> getTwoWayMessage(String user1, String user2, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        messageRepository.maskAsReal(user2, user1, MessageStatus.READ);

        Page<Message> messageList = messageRepository
                .findTwoWayMessage(user1, user2, pageable);

        Page<MessageDTO> messageDTOS = messageList.map(MessageMapper::toChatMessageDTO);

        return ApiResponse.success("List of chat messages two way", messageDTOS);
    }

    public ApiResponse<Page<MessageForGroupChatDTO>> getMessagesFromChatRoom(Long userId,Long chatRoomId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").ascending());

        Page<Message> messages = messageRepository.findMessageGroupChatByUserIdAndChatRoomId(userId, chatRoomId, pageable);

        Page<MessageForGroupChatDTO> messageDTOS = messages.map(MessageMapper::toGroupChatDTO);

        return ApiResponse.success(" messages of group chat", messageDTOS);
    }

    //chat 1:1
    public ApiResponse<MessageDTO> saveChatMessage(MessageDTO messageDTO) {
        User sender = userRepository.findByUsername(messageDTO.getSender())
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

        User recipient = userRepository.findByUsername(messageDTO.getRecipient())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));

        Message message = new Message();
        message.setSender(sender);
        message.setRecipient(recipient);

        String roomName = sender.getUsername() + "_" + recipient.getUsername(); //room name: sender-name_recipient-name
                                                                                // (người gửi tin trước (nguời tạo phòng))
        ChatRoom room = chatRoomRepository.findByRoomName(roomName)
                .orElseGet(() -> {
                    ChatRoom chatRoom = ChatRoom.builder()
                            .members(Set.of(sender, recipient))
                            .chatRoomName(roomName)
                            .owner(sender)
                            .type(ChatRoomType.CHAT_1VS1)
                            .build();

                    return chatRoomRepository.save(chatRoom);
                });

        message.setChatRoom(room);
        message.setMessage(messageDTO.getMessage());

        if (sender.getUsername().equals("admin")) {
            message.setFromAdmin(true);
        }

        message.setStatus(MessageStatus.SEND);
        message.setTimestamp(new Timestamp(System.currentTimeMillis()));

        messageRepository.save(message);

//        Map<String,Object> notifyMess = Map.of(
//                "TYPE",message.getStatus(),
//                "FROM",message.getSender().getUsername(),
//                "preview",message.getMessage()
//        );
//
//        simpMessagingTemplate.convertAndSendToUser(
//                recipient.getUsername(),
//                "/queue/message",
//                notifyMess);

        MessageDTO messageDTOResponse = MessageMapper.toChatMessageDTO(message);

        return ApiResponse.success("Chat message saved", messageDTOResponse);
    }

    //group chat
    public ApiResponse<MessageForGroupChatDTO> saveMessageForChatGroup( MessageForChatRoomRequest request){
        User user = userRepository.findByUsername(request.getSender())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ChatRoom chatRoom = chatRoomRepository.findById(request.getChatRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));

        Message message = Message.builder()
                .chatRoom(chatRoom)
                .message(request.getMessage())
                .status(MessageStatus.SEND)
                .sender(user)
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .build();

        User admin = userRepository.findByUsername("admin")
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));
        if(user.getId().equals(admin.getId())){
            message.setFromAdmin(true);
        }

        messageRepository.save(message);

        return ApiResponse.success("Chat group message saved", MessageMapper.toGroupChatDTO(message));
    }
}
