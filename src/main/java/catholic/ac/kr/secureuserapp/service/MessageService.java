package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.Status.MessageStatus;
import catholic.ac.kr.secureuserapp.exception.ResourceNotFoundException;
import catholic.ac.kr.secureuserapp.mapper.MessageMapper;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.MessageDTO;
import catholic.ac.kr.secureuserapp.model.entity.Message;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.MessageRepository;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public ApiResponse<Page<MessageDTO>> getOneWayMessage(String sender, String recipient, int pae, int size) {
        Pageable pageable = PageRequest.of(pae, size, Sort.by("timestamp").descending());
        Page<Message> messages = messageRepository.findBySenderUsernameAndRecipientUsername(sender, recipient, pageable);

        Page<MessageDTO> messageDTOS = messages.map(MessageMapper::toChatMessageDTO);

        return ApiResponse.success("List of chat messages one way", messageDTOS);
    }

    @Transactional
    public ApiResponse<Page<MessageDTO>> getTwoWayMessage(String user1, String user2, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        messageRepository.maskAsReal(user2,user1,MessageStatus.READ);

        Page<Message> messageList = messageRepository
                .findTwoWayMessage(user1, user2, pageable);

        Page<MessageDTO> messageDTOS = messageList.map(MessageMapper::toChatMessageDTO);

        return ApiResponse.success("List of chat messages two way", messageDTOS);
    }

    public ApiResponse<MessageDTO> saveChatMessage(MessageDTO messageDTO) {
        User sender = userRepository.findByUsername(messageDTO.getSender())
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

        User recipient = userRepository.findByUsername(messageDTO.getRecipient())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));

        Message message = new Message();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setMessage(messageDTO.getMessage());
        message.setFromAdmin(messageDTO.isFromAdmin());

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

    public ApiResponse<MessageDTO> updateMessageStatus(Long chatMessageId, MessageStatus messageStatus) {
        Message message = messageRepository.findById(chatMessageId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat message not found"));

        message.setStatus(messageStatus);
        messageRepository.save(message);

        MessageDTO messageDTOResponse = MessageMapper.toChatMessageDTO(message);

        return ApiResponse.success("Chat message updated", messageDTOResponse);
    }
}
