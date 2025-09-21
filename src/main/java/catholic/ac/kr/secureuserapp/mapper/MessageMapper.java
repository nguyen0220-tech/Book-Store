package catholic.ac.kr.secureuserapp.mapper;

import catholic.ac.kr.secureuserapp.model.dto.MessageDTO;
import catholic.ac.kr.secureuserapp.model.entity.Message;

public class MessageMapper {
    public static MessageDTO toChatMessageDTO(Message message) {
        MessageDTO messageDTO = new MessageDTO();

        messageDTO.setId(message.getId());
        messageDTO.setSender(message.getSender().getUsername());
        messageDTO.setSenderFullName(message.getSender().getFullName());
        messageDTO.setRecipient(message.getRecipient().getUsername());
        messageDTO.setRecipientFullName(message.getRecipient().getFullName());
        messageDTO.setMessage(message.getMessage());
        messageDTO.setFromAdmin(message.isFromAdmin());
        messageDTO.setTimestamp(message.getTimestamp());
        messageDTO.setStatus(message.getStatus().name());

        return messageDTO;
    }

}
