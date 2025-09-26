package catholic.ac.kr.secureuserapp.mapper;

import catholic.ac.kr.secureuserapp.model.dto.MessageReplyDTO;
import catholic.ac.kr.secureuserapp.model.entity.MessageReply;

public class MessageReplyMapper {
    public static MessageReplyDTO convertToDTO(MessageReply messageReply) {
        MessageReplyDTO messageReplyDTO = new MessageReplyDTO();

        messageReplyDTO.setMessageId(messageReply.getMessage().getId());
        messageReplyDTO.setReplyUser(messageReply.getUser().getFullName());
        messageReplyDTO.setMessageReply(messageReply.getMessage().getMessage());
        messageReplyDTO.setMessageReply(messageReply.getMessageReply());

        return messageReplyDTO;
    }
}
