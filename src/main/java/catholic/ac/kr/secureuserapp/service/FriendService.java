package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.Status.FriendStatus;
import catholic.ac.kr.secureuserapp.exception.ResourceNotFoundException;
import catholic.ac.kr.secureuserapp.mapper.FriendMapper;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.FriendDTO;
import catholic.ac.kr.secureuserapp.model.entity.Friend;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.FriendRepository;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendService {
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ApiResponse<Page<FriendDTO>> getFriends(Long userId,int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Friend> friends = friendRepository.findByUserIdAndStatus(userId,FriendStatus.FRIEND,pageable);

        Page<FriendDTO> friendDTOS = friends.map(FriendMapper::toFriendDTO);

        return ApiResponse.success("All friends", friendDTOS);
    }

    public ApiResponse<Page<FriendDTO>> getPendingFriendRequests(Long userId,int page, int size) {
        Pageable pageable = PageRequest.of(page, size,Sort.by("createdAt").descending());
        Page<Friend> pendingRequests = friendRepository.findByFriendIdAndStatus(userId,FriendStatus.PENDING,pageable);

        Page<FriendDTO> friendDTOS = pendingRequests.map(FriendMapper::toFriendDTO);

        return ApiResponse.success("All pending request", friendDTOS);
    }

    public ApiResponse<Page<FriendDTO>> getBlockingFriend(Long userId,int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Friend> blockingFriend = friendRepository.findByUserIdAndStatus(userId,FriendStatus.BLOCKED,pageable);

        Page<FriendDTO> friendDTOS = blockingFriend.map(FriendMapper::toFriendDTO);

        return ApiResponse.success("All blocking request", friendDTOS);

    }

    public ApiResponse<FriendDTO> addFriend(Long userId, Long friendId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getId().equals(friendId)){
            return ApiResponse.error("can not add friend/không thể thêm bạn bè với chính mình");
        }

        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend not found"));

        boolean isFriend = friendRepository.existsByUserIdAndFriendIdAndStatus(user.getId(), friend.getId(),FriendStatus.FRIEND);
        boolean isPending = friendRepository.existsByUserIdAndFriendIdAndStatus(user.getId(), friend.getId(),FriendStatus.PENDING);
        boolean isBlocking = friendRepository.existsByUserIdAndFriendIdAndStatus(user.getId(), friend.getId(),FriendStatus.BLOCKED);

        if (isFriend) {
            return ApiResponse.error("Friend already exists/các bạn đã la bạn bè");
        }

        if (isPending) {
            return ApiResponse.error("Pending friend already exists/bạn đã gửi kết bạn cho người này rồi");
        }

        if (isBlocking) {
            return ApiResponse.error("Blocking friend/không thể kết bạn vì người này có trong danh sách bị chặn");
        }

        Friend addFriend = Friend.builder()
                .user(user)
                .friend(friend)
                .status(FriendStatus.PENDING)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();

        friendRepository.save(addFriend);

        notificationService.createRequestFriendNotification(user.getId(),friendId);

        FriendDTO friendDTO = FriendMapper.toFriendDTO(addFriend);

        return ApiResponse.success("send claim friend", friendDTO);
    }

    public ApiResponse<String> acceptFriendRequest(Long currentUserId, Long senderId) {
        Friend request = friendRepository.findByUserIdAndFriendIdAndStatus(currentUserId, senderId,FriendStatus.PENDING)
                .orElseThrow(() -> new ResourceNotFoundException("Friend request not found"));

        request.setStatus(FriendStatus.FRIEND);

        Friend reciprocal = Friend.builder()
                .user(request.getFriend())
                .friend(request.getUser())
                .status(FriendStatus.FRIEND)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();

        friendRepository.save(request);
        friendRepository.save(reciprocal);

        return ApiResponse.success("Friend request accepted");
    }

    public ApiResponse<String> cancelFriendRequest(Long currentUserId, Long senderId) {
        Friend request = friendRepository.findByUserIdAndFriendIdAndStatus(currentUserId, senderId,FriendStatus.PENDING)
                .orElseThrow(() -> new ResourceNotFoundException("Friend request not found"));

        request.setStatus(FriendStatus.CANCELLED);

        friendRepository.save(request);

        return ApiResponse.success("cancelled claim friend");
    }

    @Transactional
    public ApiResponse<String> deleteFriend(Long userId, Long friendId) {
        List<Friend> friend = friendRepository.findByUserIdAndFriendIdAndStatusFRIEND(userId,friendId,FriendStatus.FRIEND);

        friendRepository.deleteAll(friend);

        return ApiResponse.success("friend deleted successfully");
    }

    public ApiResponse<String> blockFriend(Long userId, Long friendId ) {
        List<Friend> friend = friendRepository.findByUserIdAndFriendIdAndStatusFRIEND(userId, friendId,FriendStatus.FRIEND);

        for (Friend f : friend) {
            f.setStatus(FriendStatus.BLOCKED);
            friendRepository.save(f);
        }

        return ApiResponse.success("blocked friend");

    }

    public ApiResponse<String> unBlockFriend(Long userId, Long friendId) {
        List<Friend> blockingFriend = friendRepository.findByUserIdAndFriendIdAndStatusBLOCKED(userId,friendId,FriendStatus.BLOCKED);

        for (Friend f : blockingFriend) {
            f.setStatus(FriendStatus.CANCELLED);
            friendRepository.save(f);
        }

        return ApiResponse.success("un-block friend");
    }

    public ApiResponse<Integer> countFriends(Long userId) {
        int count = friendRepository.countByUserIdAndStatus(userId, FriendStatus.FRIEND);
        if (count == 0) {
            return ApiResponse.success("no friends");
        }

        return ApiResponse.success("friends count" , count);
    }
}
