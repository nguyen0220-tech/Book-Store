package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.Status.ChatRoomType;
import catholic.ac.kr.secureuserapp.exception.ResourceNotFoundException;
import catholic.ac.kr.secureuserapp.mapper.ChatRoomMapper;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.ChatRoomDTO;
import catholic.ac.kr.secureuserapp.model.dto.request.ChatRoomRequest;
import catholic.ac.kr.secureuserapp.model.entity.ChatRoom;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.ChatRoomRepository;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    public ApiResponse<Page<ChatRoomDTO>> getChatRoomsByUserId(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Pageable pageable = PageRequest.of(page, size);

        Page<ChatRoom> chatRooms = chatRoomRepository.findByUserId(user.getId(), String.valueOf(ChatRoomType.GROUP_CHAT), pageable);

        Page<ChatRoomDTO> chatRoomDTOS = chatRooms.map(ChatRoomMapper::convertToDTO);

        return ApiResponse.success("success", chatRoomDTOS);
    }

    public ApiResponse<String> createChatRoom(Long userId, ChatRoomRequest request) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("user not found"));

        ChatRoom chatRoom = new ChatRoom();

        Set<User> members = new HashSet<>(userRepository.findAllById(request.getMemberIds()));
        members.add(owner);

        chatRoom.setOwner(owner);
        chatRoom.setMembers(members);
        chatRoom.setChatRoomName(request.getChatRoomName());
        chatRoom.setType(ChatRoomType.GROUP_CHAT);

        chatRoomRepository.save(chatRoom);

        return ApiResponse.success("Chat room created");
    }

    //chỉ cho phép chủ phòng
    public ApiResponse<String> setMemberToChatRoom(Long userId, Long chatRoomId, Long memberId, Boolean act) {
        boolean success = setUserForChatRoom(userId, chatRoomId, memberId, act);

        if (success) {
            return ApiResponse.success("success");
        }

        return ApiResponse.error("fail");
    }

    private boolean setUserForChatRoom(Long userId, Long chatRoomId, Long memberId, boolean act) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("user not found"));

        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("user not found"));

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("chatRoom not found"));

        if (currentUser.getId().equals(chatRoom.getOwner().getId())) {
            //true : add    false : remove
            if (act) {
                chatRoom.getMembers().add(member);
            } else {
                chatRoom.getMembers().remove(member);
            }
            chatRoomRepository.save(chatRoom);
            return true;
        }
        return false;

    }

    //ai cũng có thể rời
    public ApiResponse<String> exitChatRoom(Long userId, Long chatRoomId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("user not found"));

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("chatRoom not found"));

        chatRoom.getMembers().remove(user);

        chatRoomRepository.save(chatRoom);

        return ApiResponse.success("success");

    }

    public ApiResponse<String> renameChatRoom(Long chatRoomId, String newName) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("chatRoom not found"));

        chatRoom.setChatRoomName(newName);

        chatRoomRepository.save(chatRoom);

        return ApiResponse.success(newName);
    }

    //chỉ cho phép chủ phòng
    public ApiResponse<String> deleteChatRoom(Long userId, Long chatRoomId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("user not found"));

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ResourceNotFoundException("chatRoom not found"));

        if (!user.getId().equals(chatRoom.getOwner().getId())) {
            return ApiResponse.error("cannot delete chat room");
        }

        chatRoom.setDeleted(true);

        chatRoomRepository.save(chatRoom);

        return ApiResponse.success("delete success");
    }
}

//    private ChatRoom getCHatRoom(Long userId, Long chatRoomId) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException("user not found"));
//
//        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
//                .orElseThrow(() -> new ResourceNotFoundException("chatRoom not found"));
//
//
//    }
//}
