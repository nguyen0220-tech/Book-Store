package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.exception.ResourceNotFoundException;
import catholic.ac.kr.secureuserapp.mapper.BookMarkMapper;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.BookMarkDTO;
import catholic.ac.kr.secureuserapp.model.entity.Book;
import catholic.ac.kr.secureuserapp.model.entity.BookMark;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.BookMarkRepository;
import catholic.ac.kr.secureuserapp.repository.BookRepository;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookMarkService {
    private final BookMarkRepository bookMarkRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public ApiResponse<List<BookMarkDTO>> getAllBookMarks(Long userId) {
        List<BookMark> bookMarks = bookMarkRepository.findByUserId(userId);

        List<BookMarkDTO> dto = bookMarks.stream()
                .map(BookMarkMapper::toBookMarkDTO)
                .toList();

        return ApiResponse.success("Bookmark", dto);
    }


    public ApiResponse<String> addBookMark(Long userId, Long bookId) {
        if (bookMarkRepository.existsByUserIdAndBookId(userId,bookId)) {
            return ApiResponse.error("Bookmark already exists");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        BookMark bookMark = BookMark.builder()
                .user(user)
                .book(book)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();

        bookMarkRepository.save(bookMark);

        return ApiResponse.success("Bookmark added");
    }

    @Transactional
    public ApiResponse<String> removeBookMark(Long userId, Long bookId) {
        bookMarkRepository.deleteByUserIdAndBookId(userId,bookId);
        return ApiResponse.success("Bookmark removed");
    }
}
