package catholic.ac.kr.secureuserapp.mapper;

import catholic.ac.kr.secureuserapp.model.dto.MessageDTO;
import catholic.ac.kr.secureuserapp.model.dto.MessageForGroupChatDTO;
import catholic.ac.kr.secureuserapp.model.dto.MessageReplyDTO;
import catholic.ac.kr.secureuserapp.model.entity.Message;

import java.util.List;

public class MessageMapper {
    public static MessageDTO toChatMessageDTO(Message message) {
        MessageDTO messageDTO = new MessageDTO();

        messageDTO.setId(message.getId());
        messageDTO.setSender(message.getSender().getUsername());
        messageDTO.setSenderFullName(message.getSender().getFullName());
        if (message.getRecipient() != null) {
            messageDTO.setRecipient(message.getRecipient().getUsername());
            messageDTO.setRecipientFullName(message.getRecipient().getFullName());
        } else {
            messageDTO.setRecipient(null);
            messageDTO.setRecipientFullName(null);
        }
        messageDTO.setMessage(message.getMessage());
        messageDTO.setTimestamp(message.getTimestamp());
        messageDTO.setStatus(message.getStatus().name());

        return messageDTO;
    }

    public static MessageForGroupChatDTO toGroupChatDTO(Message message) {
        MessageForGroupChatDTO messageForGroupChatDTO = new MessageForGroupChatDTO();

        messageForGroupChatDTO.setSenderFullName(message.getSender().getFullName());
        messageForGroupChatDTO.setMessageId(message.getId());
        messageForGroupChatDTO.setMessage(message.getMessage());
        messageForGroupChatDTO.setTimestamp(message.getTimestamp());

        if (message.getMessageReplies() != null) {
            List<MessageReplyDTO> replies = message.getMessageReplies().stream()
                    .map( r ->{
                        MessageReplyDTO messageReplyDTO = new MessageReplyDTO();

                        messageReplyDTO.setReplyUser(r.getUser().getFullName());
                        messageReplyDTO.setMessageId(r.getMessage().getId());
                        messageReplyDTO.setMessageReply(r.getMessageReply());
                        messageReplyDTO.setMessageReply(r.getMessageReply());

                        return messageReplyDTO;
                    })
                    .toList();

            messageForGroupChatDTO.setMessageReplies(replies);

        }

        return messageForGroupChatDTO;
    }

}
