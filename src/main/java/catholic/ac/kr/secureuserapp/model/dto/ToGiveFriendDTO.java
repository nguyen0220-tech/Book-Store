package catholic.ac.kr.secureuserapp.model.dto;

public record ToGiveFriendDTO(
        String recipientName,
        String friendPhone,
        String friendAddress
) {
}
