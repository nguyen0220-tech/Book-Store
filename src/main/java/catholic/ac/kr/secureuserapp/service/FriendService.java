package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.Status.FriendStatus;
import catholic.ac.kr.secureuserapp.exception.ResourceNotFoundException;
import catholic.ac.kr.secureuserapp.mapper.FriendMapper;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.FriendChatDTO;
import catholic.ac.kr.secureuserapp.model.dto.FriendDTO;
import catholic.ac.kr.secureuserapp.model.dto.ToGiveFriendDTO;
import catholic.ac.kr.secureuserapp.model.entity.Friend;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.FriendRepository;
import catholic.ac.kr.secureuserapp.repository.MessageRepository;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FriendService {
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final MessageRepository messageRepository;

    public ApiResponse<Page<FriendDTO>> getFriends(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Friend> friends = friendRepository.findByUserIdAndStatus(userId, FriendStatus.FRIEND, pageable);

        Page<FriendDTO> friendDTOS = friends.map(FriendMapper::toFriendDTO);

        return ApiResponse.success("All friends", friendDTOS);
    }

    public ApiResponse<Page<FriendDTO>> getPendingFriendRequests(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Friend> pendingRequests = friendRepository.findByFriendIdAndStatus(userId, FriendStatus.PENDING, pageable);

        Page<FriendDTO> friendDTOS = pendingRequests.map(FriendMapper::toFriendDTO);

        return ApiResponse.success("All pending request", friendDTOS);
    }

    public ApiResponse<Page<FriendDTO>> getBlockingFriend(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Friend> blockingFriend = friendRepository.findByUserIdAndStatus(userId, FriendStatus.BLOCKED, pageable);

        Page<FriendDTO> friendDTOS = blockingFriend.map(FriendMapper::toFriendDTO);

        return ApiResponse.success("All blocking request", friendDTOS);

    }

    public ApiResponse<List<ToGiveFriendDTO>> getToGiveFriends(Long userId) {
        List<ToGiveFriendDTO> list = friendRepository.findToGiveFriends(userId);

        return ApiResponse.success("All to give friends", list);
    }

    public ApiResponse<FriendDTO> addFriend(Long userId, Long friendId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getId().equals(friendId)) {
            return ApiResponse.error("can not add friend/không thể thêm bạn bè với chính mình");
        }

        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend not found"));

        boolean isFriend = friendRepository.existsByUserIdAndFriendIdAndStatus(user.getId(), friend.getId(), FriendStatus.FRIEND);
        boolean isPending = friendRepository.existsByUserIdAndFriendIdAndStatus(user.getId(), friend.getId(), FriendStatus.PENDING);
        boolean isBlocking = friendRepository.existsByUserIdAndFriendIdAndStatus(user.getId(), friend.getId(), FriendStatus.BLOCKED);
        boolean isBlockedFrom = friendRepository.existsByUserIdAndFriendIdAndStatus(user.getId(), friend.getId(), FriendStatus.BLOCKED_FROM);

        if (isFriend) {
            return ApiResponse.error("Friend already exists/các bạn đã la bạn bè");
        }

        if (isPending) {
            return ApiResponse.error("Pending friend already exists/bạn đã gửi kết bạn cho người này rồi");
        }

        if (isBlocking) {
            return ApiResponse.error("Blocking friend/không thể kết bạn vì người này có trong danh sách bị chặn");
        }

        if (isBlockedFrom) {
            return ApiResponse.error("không thể kết bạn vì người này đã chặn bạn");
        }

        Friend addFriend = Friend.builder()
                .user(user)
                .friend(friend)
                .status(FriendStatus.PENDING)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();

        friendRepository.save(addFriend);

        notificationService.createRequestFriendNotification(user.getId(), friendId);

        FriendDTO friendDTO = FriendMapper.toFriendDTO(addFriend);

        return ApiResponse.success("send claim friend", friendDTO);
    }

    public ApiResponse<String> acceptFriendRequest(Long currentUserId, Long senderId) {
        Friend request = friendRepository.findByUserIdAndFriendIdAndStatus(currentUserId, senderId, FriendStatus.PENDING)
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
        Friend request = friendRepository.findByUserIdAndFriendIdAndStatus(currentUserId, senderId, FriendStatus.PENDING)
                .orElseThrow(() -> new ResourceNotFoundException("Friend request not found"));

        request.setStatus(FriendStatus.CANCELLED);

        friendRepository.save(request);

        return ApiResponse.success("cancelled claim friend");
    }

    @Transactional
    public ApiResponse<String> deleteFriend(Long userId, Long friendId) {
        List<Friend> friend = friendRepository.findByUserIdAndFriendIdAndStatusTwoWay(userId, friendId, FriendStatus.FRIEND);

        friendRepository.deleteAll(friend);

        return ApiResponse.error("friend deleted");
    }

    public ApiResponse<String> blockFriend(Long userId, Long friendId) {
        Optional<Friend> friend = friendRepository.findByUserIdAndFriendIdAndStatusOneWay(userId, friendId, FriendStatus.FRIEND);
        Optional<Friend> friendBlockFrom = friendRepository.findByUserIdAndFriendIdAndStatusOneWay(friendId, userId, FriendStatus.FRIEND);

        if (friend.isPresent() && friendBlockFrom.isPresent()) {
            friend.get().setStatus(FriendStatus.BLOCKED);
            friendRepository.save(friend.get());

            friendBlockFrom.get().setStatus(FriendStatus.BLOCKED_FROM);
            friendRepository.save(friendBlockFrom.get());

            return ApiResponse.success("blocked friend");
        }
        else
            return ApiResponse.success("friend not found");
    }

    public ApiResponse<String> unBlockFriend(Long userId, Long friendId) {
        Optional<Friend> blockingFriend = friendRepository.findByUserIdAndFriendIdAndStatusOneWay(userId, friendId, FriendStatus.BLOCKED);
        Optional<Friend> blockedFromFriend = friendRepository.findByUserIdAndFriendIdAndStatusOneWay(friendId, userId, FriendStatus.BLOCKED_FROM);

        if (blockingFriend.isPresent() && blockedFromFriend.isPresent()) {
            blockingFriend.get().setStatus(FriendStatus.CANCELLED);
            friendRepository.save(blockingFriend.get());

            blockedFromFriend.get().setStatus(FriendStatus.CANCELLED);
            friendRepository.save(blockedFromFriend.get());

            return ApiResponse.success("un-block friend");
        } else
            return ApiResponse.success("friend not found");
    }

    public ApiResponse<Integer> countFriends(Long userId) {
        int count = friendRepository.countByUserIdAndStatus(userId, FriendStatus.FRIEND);
        if (count == 0) {
            return ApiResponse.success("no friends");
        }

        return ApiResponse.success("friends count", count);
    }

    public ApiResponse<Integer> countRequestFriends(Long userId) {
        int count = friendRepository.countRequestFriendByUserId(userId);
        if (count == 0) {
            return ApiResponse.success("no friends request");
        }

        return ApiResponse.success("friends request count", count);
    }

    public ApiResponse<Page<FriendDTO>> getSendRequestFriend(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Friend> friendPage = friendRepository.findByUserIdAndStatus(userId, FriendStatus.PENDING, pageable);

        Page<FriendDTO> dtoPage = friendPage.map(FriendMapper::toFriendDTO);

        return ApiResponse.success("find send request friend", dtoPage);
    }

    public ApiResponse<Page<FriendChatDTO>> getAllFriendAndAdminToChatMessage(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<FriendChatDTO> dtoPage = friendRepository.findByUserId(userId, pageable);

        User admin = userRepository.findByUsername("admin")
                .orElseThrow(() -> new ResourceNotFoundException("admin not found"));

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("current user not found"));

        boolean isFriendWithAdmin = friendRepository.existsByUserIdAndFriendIdAndStatus(userId,admin.getId(),FriendStatus.FRIEND);

        if ( isFriendWithAdmin) {
            return ApiResponse.success("find send request friend", dtoPage);
        }

        Page<FriendChatDTO> userSendToAdmin = messageRepository.findUsersSendMessageToAdmin(admin.getId(), pageable);

        if (currentUser.getId().equals(admin.getId())) {
            return ApiResponse.success("find send request friend", userSendToAdmin);
        }

        FriendChatDTO adminChat = new FriendChatDTO(admin.getId(),admin.getUsername());

        List<FriendChatDTO> dtoList = new ArrayList<>(dtoPage.getContent());

        long totalPages = dtoPage.getTotalPages();
        if (page == 0){
            dtoList.add(adminChat);
            totalPages ++;
        }

        Page<FriendChatDTO> result = new PageImpl<>(dtoList, pageable, totalPages);

        return ApiResponse.success("success",result);
        //kiem tra xem ket ban ad chua? neu roi thi khong them ad vao list
        //khong them ad khi user = admin
    }
}
