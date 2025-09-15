package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.RankDTO;
import catholic.ac.kr.secureuserapp.security.userdetails.MyUserDetails;
import catholic.ac.kr.secureuserapp.service.RankService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("rank")
public class RankController {
    private final RankService rankService;

    public RankController(RankService rankService) {
        this.rankService = rankService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<RankDTO>>> getAllRanks(
            @RequestParam int page,
            @RequestParam int size){
        return ResponseEntity.ok(rankService.getAllRank(page, size));
    }

    @GetMapping("my-rank")
    public ResponseEntity<ApiResponse<RankDTO>> getRank(@AuthenticationPrincipal MyUserDetails user){
        return ResponseEntity.ok(rankService.getRankByUserId(user.getUser().getId()));
    }
}
