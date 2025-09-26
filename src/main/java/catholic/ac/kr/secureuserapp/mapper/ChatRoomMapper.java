package catholic.ac.kr.secureuserapp.mapper;

import catholic.ac.kr.secureuserapp.model.dto.ChatRoomDTO;
import catholic.ac.kr.secureuserapp.model.entity.ChatRoom;
import catholic.ac.kr.secureuserapp.model.entity.User;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ChatRoomMapper {
    public static ChatRoomDTO convertToDTO(ChatRoom chatRoom) {
        ChatRoomDTO chatRoomDTO = new ChatRoomDTO();

        chatRoomDTO.setId(chatRoom.getId());
        chatRoomDTO.setChatRoomName(chatRoom.getChatRoomName());
//        chatRoomDTO.setUsernames(chatRoom.getMembers().stream()
//                .map(User::getUsername).collect(Collectors.toSet()));

        Map<Long,String> dtoMap = chatRoom.getMembers().stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        chatRoomDTO.setMembers(dtoMap);

        return chatRoomDTO;
    }
}
