package catholic.ac.kr.secureuserapp.mapper;

import catholic.ac.kr.secureuserapp.model.dto.FriendDTO;
import catholic.ac.kr.secureuserapp.model.entity.Friend;

import java.util.ArrayList;
import java.util.List;

public class FriendMapper {
    public static FriendDTO toFriendDTO(Friend friend) {
        FriendDTO friendDTO = new FriendDTO();

        friendDTO.setId(friend.getId());
        friendDTO.setUserId(friend.getUser().getId());
        friendDTO.setUsername(friend.getUser().getUsername());
        friendDTO.setFriendId(friend.getFriend().getId());
        friendDTO.setFriendName(friend.getFriend().getUsername());
        friendDTO.setStatus(String.valueOf(friend.getStatus()));

        return friendDTO;
    }

    public static List<FriendDTO> toFriendDTOList(List<Friend> friends) {
        List<FriendDTO> friendDTOList = new ArrayList<>();

        for (Friend friend : friends) {
            friendDTOList.add(toFriendDTO(friend));
        }
        return friendDTOList;
    }
}
