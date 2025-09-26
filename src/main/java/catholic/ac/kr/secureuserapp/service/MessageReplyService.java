package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.exception.ResourceNotFoundException;
import catholic.ac.kr.secureuserapp.mapper.MessageReplyMapper;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.MessageReplyDTO;
import catholic.ac.kr.secureuserapp.model.entity.Message;
import catholic.ac.kr.secureuserapp.model.entity.MessageReply;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.MessageRepository;
import catholic.ac.kr.secureuserapp.repository.MessageReplyRepository;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageReplyService {
    private final MessageReplyRepository messageReplyRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    public ApiResponse<MessageReplyDTO> createMessageReply(Long userId, Long messageId, String replyText) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        Message message = messageRepository.findById(messageId)
                .orElseThrow(()-> new ResourceNotFoundException("Message not found"));

        MessageReply messageReply = new MessageReply();

        messageReply.setUser(user);
        messageReply.setMessage(message);
        messageReply.setMessageReply(replyText);

        messageReplyRepository.save(messageReply);

        MessageReplyDTO messageReplyDTO = MessageReplyMapper.convertToDTO(messageReply);
        return ApiResponse.success("Reply message", messageReplyDTO);
    }
}
