package catholic.ac.kr.secureuserapp.mapper;

import catholic.ac.kr.secureuserapp.model.dto.ChatMessageDTO;
import catholic.ac.kr.secureuserapp.model.entity.ChatMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

public class ChatMessageMapper {
    public static ChatMessageDTO toChatMessageDTO(ChatMessage chatMessage) {
        ChatMessageDTO chatMessageDTO = new ChatMessageDTO();

        chatMessageDTO.setId(chatMessage.getId());
        chatMessageDTO.setSender(chatMessage.getSender().getUsername());
        chatMessageDTO.setSenderFullName(chatMessage.getSender().getFullName());
        chatMessageDTO.setRecipient(chatMessage.getRecipient().getUsername());
        chatMessageDTO.setRecipientFullName(chatMessage.getRecipient().getFullName());
        chatMessageDTO.setMessage(chatMessage.getMessage());
        chatMessageDTO.setFromAdmin(chatMessage.isFromAdmin());
        chatMessageDTO.setTimestamp(chatMessage.getTimestamp());
        chatMessageDTO.setStatus(chatMessage.getStatus().name());

        return chatMessageDTO;
    }

    public List<ChatMessageDTO> toChatMessageDTO(List<ChatMessage> chatMessages) {
        if (chatMessages == null || chatMessages.isEmpty()) {
            return null;
        }

        List<ChatMessageDTO> chatMessageDTOs = new ArrayList<ChatMessageDTO>();
        for (ChatMessage chatMessage : chatMessages) {
            chatMessageDTOs.add(toChatMessageDTO(chatMessage));
        }
        return chatMessageDTOs;
    }
}
