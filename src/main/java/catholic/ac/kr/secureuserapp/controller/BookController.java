package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.model.dto.*;
import catholic.ac.kr.secureuserapp.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("book")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @GetMapping("{bookId}")
    public ResponseEntity<ApiResponse<BookDTO>> getBook(@PathVariable("bookId") Long bookId) {
        return ResponseEntity.ok(bookService.getBookById(bookId));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BookDTO>>> findAllBooks(
            @RequestParam int page,
            @RequestParam int size
    ) {
        return ResponseEntity.ok(bookService.getAllBooks(page, size));
    }

    @GetMapping("by-author")
    public ResponseEntity<ApiResponse<Page<BookDTO>>> findBookByAuthor(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String author
    ) {
        return ResponseEntity.ok(bookService.getAllBooksByAuthor(page, size, author));
    }

    @GetMapping("by-category")
    public ResponseEntity<ApiResponse<Page<BookDTO>>> findBookByCategory(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String category
    ) {
        return ResponseEntity.ok(bookService.getAllBooksByCategory(page, size, category));
    }

    @GetMapping("by-title")
    public ResponseEntity<ApiResponse<Page<BookDTO>>> findBookByTitle(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String title
    ) {
        return ResponseEntity.ok(bookService.getAllBooksByTitle(page, size, title));
    }

    @GetMapping("random")
    public ResponseEntity<ApiResponse<Page<BookDTO>>> getRandomBooks(
            @RequestParam int page,
            @RequestParam int size
    ){
        return ResponseEntity.ok(bookService.getRandomBooks(page, size));
    }

    @GetMapping("top-book")
    public ResponseEntity<ApiResponse<List<TopBookDTO>>> getTopBooks(){
        return ResponseEntity.ok(bookService.getTopBooks());
    }

    @GetMapping("top-new")
    public ResponseEntity<ApiResponse<List<TopBookDTO>>> getTopNewBooks(){
        return ResponseEntity.ok(bookService.getTopNewBooks());
    }

    @GetMapping("stock-max50")
    public ResponseEntity<ApiResponse<Page<BookStockMax50DTO>>> getBooksStockMax50(
            @RequestParam int page,
            @RequestParam int size){
        return ResponseEntity.ok(bookService.getBooksStockMax50(page,size));
    }

    @PostMapping("add")
    public ResponseEntity<ApiResponse<BookDTO>> createBook(@Valid @RequestBody CreateBookRequest request){
        return ResponseEntity.ok(bookService.createBook(request));
    }

    @PostMapping("{id}")
    public ResponseEntity<ApiResponse<BookDTO>> updateBook(@PathVariable("id") Long id,@RequestBody BookDTO bookDTO){
        return ResponseEntity.ok(bookService.updateBook(id, bookDTO));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<ApiResponse<String>> deleteBook(@PathVariable("id") Long id){
        return ResponseEntity.ok(bookService.deleteBook(id));
    }
}
