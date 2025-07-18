package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.SearchHistoryDTO;
import catholic.ac.kr.secureuserapp.model.dto.SearchHistoryRequest;
import catholic.ac.kr.secureuserapp.security.userdetails.MyUserDetails;
import catholic.ac.kr.secureuserapp.service.SearchHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("search-history")
@RequiredArgsConstructor
public class SearchHistoryController {
    private final SearchHistoryService searchHistoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SearchHistoryDTO>>> searchHistory(@AuthenticationPrincipal MyUserDetails user) {
        return ResponseEntity.ok(searchHistoryService.getAllSearchHistory(user.getUser().getId()));
    }

    @PostMapping("add")
    public ResponseEntity<ApiResponse<String>> saveSearchHistory(
            @AuthenticationPrincipal MyUserDetails user,
            @RequestBody SearchHistoryRequest request) {
        return ResponseEntity.ok(searchHistoryService.saveSearchHistory(
                user.getUser().getId(), request.getKeyword(), request.getType()));
    }

    @DeleteMapping("delete")
    public ResponseEntity<ApiResponse<String>> deleteSearchHistory(
            @AuthenticationPrincipal MyUserDetails user,
            @RequestParam String keyword) {
        return ResponseEntity.ok(searchHistoryService.deleteSearchHistory(user.getUser().getId(), keyword));
    }

    @DeleteMapping("delete-all")
    public ResponseEntity<ApiResponse<String>> deleteAllSearchHistory(@AuthenticationPrincipal MyUserDetails user) {
        return ResponseEntity.ok(searchHistoryService.deleteAllSearchHistory(user.getUser().getId()));
    }


}