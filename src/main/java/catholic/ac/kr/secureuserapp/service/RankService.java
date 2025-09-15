package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.Status.RankStatus;
import catholic.ac.kr.secureuserapp.exception.ResourceNotFoundException;
import catholic.ac.kr.secureuserapp.mapper.RankMapper;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.RankDTO;
import catholic.ac.kr.secureuserapp.model.dto.UserPaymentAmount;
import catholic.ac.kr.secureuserapp.model.entity.Rank;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.OrderRepository;
import catholic.ac.kr.secureuserapp.repository.RankRepository;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RankService {
    private final RankRepository rankRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<RankDTO>> getAllRank(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Rank> ranks = rankRepository.findAll(pageable);
        Page<RankDTO> rankDTOS = ranks.map(RankMapper::toRankDTO);

        return ApiResponse.success("all ranks", rankDTOS);
    }

    public ApiResponse<RankDTO> getRankByUserId(Long userId) {
        Rank rank = rankRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Rank not found"));

        RankDTO rankDTO = RankMapper.toRankDTO(rank);

        return ApiResponse.success("rank of user", rankDTO);

    }

    public void updateRankToUser() {
        List<UserPaymentAmount> list = orderRepository.getAllUserPaymentAmounts().stream()
                .filter(l -> l.totalAmount().compareTo(BigDecimal.valueOf(1000000)) > 0)
                .toList();

        for (UserPaymentAmount userPaymentAmount : list) {
            System.out.println(userPaymentAmount.username());
            User user = userRepository.findByUsername(userPaymentAmount.username())
                    .orElseThrow(() -> new RuntimeException("user not found"));

            boolean isSet = rankRepository.existsByUser(user);

            if (isSet) {
                Rank rank = rankRepository.findByUser(user);

                if (rank.getRank() == RankStatus.DIAMOND)
                    continue;
                setRank(userPaymentAmount, rank);

                rankRepository.save(rank);

                continue;
            }

            Rank rank = new Rank();

            rank.setUser(user);
            setRank(userPaymentAmount, rank);

            rank.setSetRank(true);

            rankRepository.save(rank);
        }
    }

    private void setRank(UserPaymentAmount userPaymentAmount, Rank rank) {
        int i = userPaymentAmount.totalAmount().compareTo(BigDecimal.valueOf(2000000));
        if ((userPaymentAmount.totalAmount().compareTo(BigDecimal.valueOf(1000000))) > 0 &&
                (i <= 0)) {
            rank.setRank(RankStatus.SILVER);
        } else if (i > 0 &&
                (userPaymentAmount.totalAmount().compareTo(BigDecimal.valueOf(4000000)) <= 0)) {
            rank.setRank(RankStatus.GOLD);
        } else
            rank.setRank(RankStatus.DIAMOND);
    }
}
