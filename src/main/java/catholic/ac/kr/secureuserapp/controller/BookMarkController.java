package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.BookMarkDTO;
import catholic.ac.kr.secureuserapp.security.userdetails.MyUserDetails;
import catholic.ac.kr.secureuserapp.service.BookMarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("book-mark")
@RequiredArgsConstructor
public class BookMarkController {
    private final BookMarkService bookMarkService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BookMarkDTO>>> getBookMarks(@AuthenticationPrincipal MyUserDetails userDetails) {
        return ResponseEntity.ok(bookMarkService.getAllBookMarks(userDetails.getUser().getId()));
    }

    @PostMapping("add")
    public ResponseEntity<ApiResponse<String>> addBookMark(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @RequestParam Long bookId) {
        return ResponseEntity.ok(bookMarkService.addBookMark(userDetails.getUser().getId(), bookId));
    }

    @DeleteMapping("remove")
    public ResponseEntity<ApiResponse<String>> removeBookMark(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @RequestParam Long bookId
    ){
        return ResponseEntity.ok(bookMarkService.removeBookMark(userDetails.getUser().getId(), bookId));
    }
}
