package catholic.ac.kr.secureuserapp.mapper;

import catholic.ac.kr.secureuserapp.model.dto.FriendDTO;
import catholic.ac.kr.secureuserapp.model.entity.Friend;

public class FriendMapper {
    public static FriendDTO toFriendDTO(Friend friend) {
        FriendDTO friendDTO = new FriendDTO();

        friendDTO.setId(friend.getId());
        friendDTO.setUserId(friend.getUser().getId());
        friendDTO.setUsername(friend.getUser().getFullName());
        friendDTO.setFriendId(friend.getFriend().getId());
        friendDTO.setFriendName(friend.getFriend().getFullName());
        friendDTO.setStatus(String.valueOf(friend.getStatus()));

        return friendDTO;
    }
}
