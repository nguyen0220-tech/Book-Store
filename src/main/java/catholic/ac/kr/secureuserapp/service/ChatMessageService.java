package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.Status.MessageStatus;
import catholic.ac.kr.secureuserapp.exception.ResourceNotFoundException;
import catholic.ac.kr.secureuserapp.mapper.ChatMessageMapper;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.ChatMessageDTO;
import catholic.ac.kr.secureuserapp.model.entity.ChatMessage;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.ChatMessageRepository;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    public ApiResponse<Page<ChatMessageDTO>> getOneWayMessage(String sender, String recipient, int pae, int size) {
        Pageable pageable = PageRequest.of(pae, size, Sort.by("timestamp").descending());
        Page<ChatMessage> messages = chatMessageRepository.findBySenderUsernameAndRecipientUsername(sender, recipient, pageable);

        Page<ChatMessageDTO> messageDTOS = messages.map(ChatMessageMapper::toChatMessageDTO);

        return ApiResponse.success("List of chat messages one way", messageDTOS);
    }

    public ApiResponse<Page<ChatMessageDTO>> getTwoWayMessage(String user1, String user2, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ChatMessage> messageList = chatMessageRepository
                .findTwoWayMessage(user1, user2, pageable);

        Page<ChatMessageDTO> messageDTOS = messageList.map(ChatMessageMapper::toChatMessageDTO);
        return ApiResponse.success("List of chat messages two way", messageDTOS);
    }

    public ApiResponse<ChatMessageDTO> saveChatMessage(ChatMessageDTO chatMessageDTO) {
        User sender = userRepository.findByUsername(chatMessageDTO.getSender())
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

        User recipient = userRepository.findByUsername(chatMessageDTO.getRecipient())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));

        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setMessage(chatMessageDTO.getMessage());
        message.setFromAdmin(chatMessageDTO.isFromAdmin());
        message.setStatus(MessageStatus.SEND);
        message.setTimestamp(new Timestamp(System.currentTimeMillis()));

        chatMessageRepository.save(message);

        ChatMessageDTO chatMessageDTOResponse = ChatMessageMapper.toChatMessageDTO(message);

        return ApiResponse.success("Chat message saved", chatMessageDTOResponse);
    }

    public ApiResponse<ChatMessageDTO> updateMessageStatus(Long chatMessageId, MessageStatus messageStatus) {
        ChatMessage message = chatMessageRepository.findById(chatMessageId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat message not found"));

        message.setStatus(messageStatus);
        chatMessageRepository.save(message);

        ChatMessageDTO chatMessageDTOResponse = ChatMessageMapper.toChatMessageDTO(message);

        return ApiResponse.success("Chat message updated", chatMessageDTOResponse);
    }
}
