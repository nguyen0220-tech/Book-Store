package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.exception.ResourceNotFoundException;
import catholic.ac.kr.secureuserapp.mapper.BookMapper;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.BookDTO;
import catholic.ac.kr.secureuserapp.model.dto.CreateBookRequest;
import catholic.ac.kr.secureuserapp.model.dto.TopBookDTO;
import catholic.ac.kr.secureuserapp.model.entity.Book;
import catholic.ac.kr.secureuserapp.model.entity.Category;
import catholic.ac.kr.secureuserapp.repository.BookRepository;
import catholic.ac.kr.secureuserapp.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final CategoryRepository categoryRepository;

    public ApiResponse<BookDTO> getBookById(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        BookDTO bookDTO = bookMapper.bookToBookDTO(book);

        return ApiResponse.success("Book found", bookDTO);
    }

    public ApiResponse<Page<BookDTO>> getAllBooks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookRepository.findAll(pageable);
        Page<BookDTO> bookDTOS = bookMapper.toBookDTO(books);

        return ApiResponse.success("All books found", bookDTOS);
    }

    public ApiResponse<Page<BookDTO>> getAllBooksByAuthor(int page, int size, String author) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookRepository.findByAuthor(author, pageable);
        Page<BookDTO> bookDTOS = bookMapper.toBookDTO(books);

        return ApiResponse.success("Books", bookDTOS);
    }

    public ApiResponse<Page<BookDTO>> getAllBooksByTitle(int page, int size, String title) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookRepository.findByTitle(title, pageable);
        Page<BookDTO> bookDTOS = bookMapper.toBookDTO(books);

        return ApiResponse.success("Books", bookDTOS);
    }

    public ApiResponse<Page<BookDTO>> getAllBooksByCategory(int page, int size, String category) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookRepository.findByCategory(category, pageable);
        Page<BookDTO> bookDTOS = bookMapper.toBookDTO(books);

        return ApiResponse.success("Books", bookDTOS);
    }

    public ApiResponse<Page<BookDTO>> getRandomBooks(int page, int size) {
        int total = bookRepository.countAllBooks();

        int offset = page * size;
        List<Book> randomBooks = bookRepository.findRandomBooks(size, offset);

        Pageable pageable = PageRequest.of(page, size);
        Page<Book> pageResult = new PageImpl<>(randomBooks, pageable, total);

        Page<BookDTO> bookDTOS = bookMapper.toBookDTO(pageResult);

        return ApiResponse.success("Random books", bookDTOS);
    }

    public ApiResponse<List<TopBookDTO>> getTopBooks() {
        List<TopBookDTO> topBookDTOS = bookRepository.findTop5SellingBooks(PageRequest.of(0,5));
        return ApiResponse.success("Top books", topBookDTOS);
    }

    public ApiResponse<List<TopBookDTO>> getTopNewBooks() {
        List<TopBookDTO> topBookDTOS = bookRepository.findTop5NewBooks(PageRequest.of(0,5));
        return ApiResponse.success("Top books", topBookDTOS);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BookDTO> createBook(CreateBookRequest createBookRequest) {
        Book book = bookMapper.fromCreateBook(createBookRequest);

        Category category = categoryRepository.findById(createBookRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid category"));

        book.setCategory(category);

        Book savedBook = bookRepository.save(book);

        BookDTO bookDTO = bookMapper.bookToBookDTO(savedBook);

        return ApiResponse.success("Book created", bookDTO);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BookDTO> updateBook(Long id, BookDTO bookDTO) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid book"));

        Category category = categoryRepository.findById(bookDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid category"));

        book.setTitle(bookDTO.getTitle());
        book.setAuthor(bookDTO.getAuthor());
        book.setPrice(bookDTO.getPrice());
        book.setStock(bookDTO.getStock());
        book.setDescription(bookDTO.getDescription());
        book.setImgUrl(bookDTO.getImgUrl());
        book.setCategory(category);

        Book savedBook = bookRepository.save(book);
        BookDTO updatedBookDTO = bookMapper.bookToBookDTO(savedBook);

        return ApiResponse.success("Book updated", updatedBookDTO);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> deleteBook(Long id) {
        Book book = bookRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Book not found"));

        bookRepository.delete(book);
        return ApiResponse.success("Book deleted from store");
    }
}
