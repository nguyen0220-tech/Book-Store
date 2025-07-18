package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.Status.SearchType;
import catholic.ac.kr.secureuserapp.exception.ResourceNotFoundException;
import catholic.ac.kr.secureuserapp.mapper.SearchHistoryMapper;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.SearchHistoryDTO;
import catholic.ac.kr.secureuserapp.model.entity.SearchHistory;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.SearchHistoryRepository;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchHistoryService {
    private final SearchHistoryRepository searchHistoryRepository;
    private final UserRepository userRepository;

    public ApiResponse<List<SearchHistoryDTO>> getAllSearchHistory(Long userId) {
        List<SearchHistory> searchHistoryList = searchHistoryRepository
                .find5ResultSearchHistoryByUserId(userId, PageRequest.of(0,5));

        List<SearchHistoryDTO> dtos = SearchHistoryMapper.toSearchHistoryDTOList(searchHistoryList);

        return ApiResponse.success("Search history", dtos);
    }

    public ApiResponse<String> saveSearchHistory(Long userId, String keyword, SearchType type) {
        User user = userRepository.findById(userId)
                .orElseThrow( ()-> new ResourceNotFoundException("User not found"));


        SearchHistory searchHistory = searchHistoryRepository
                .findByUserIdAndSearch(userId, keyword,type)
                .orElseGet(() -> new SearchHistory(null, keyword,type, user, new Timestamp(System.currentTimeMillis())));

        searchHistoryRepository.save(searchHistory);

        return ApiResponse.success("Search history saved");
    }

    @Transactional
    public ApiResponse<String> deleteSearchHistory(Long userId,String keyword) {
        searchHistoryRepository.deleteByUserIdAndSearch(userId,keyword);
        return ApiResponse.success("Search history deleted");
    }

    @Transactional
    public ApiResponse<String> deleteAllSearchHistory(Long userId) {
        searchHistoryRepository.deleteAllByUserId(userId);
        return ApiResponse.success("All search history deleted ");
    }
}
