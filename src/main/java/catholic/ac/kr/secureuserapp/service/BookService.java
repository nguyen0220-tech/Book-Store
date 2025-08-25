package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.exception.ResourceNotFoundException;
import catholic.ac.kr.secureuserapp.mapper.BookMapper;
import catholic.ac.kr.secureuserapp.model.dto.*;
import catholic.ac.kr.secureuserapp.model.entity.*;
import catholic.ac.kr.secureuserapp.repository.BookMarkRepository;
import catholic.ac.kr.secureuserapp.repository.BookRepository;
import catholic.ac.kr.secureuserapp.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final CategoryRepository categoryRepository;
    private final NotificationService notificationService;
    private final BookMarkRepository bookMarkRepository;

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

    public ApiResponse<Page<SuggestBooksFromFriendDTO>> getSuggestBooksFromFriend(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<SuggestBooksFromFriendDTO> pageResult = bookRepository.findSuggestBooksFromFriends(userId, pageable);

        //Phân trang -> lọc:
//        // 1. Lấy danh sách book user đã mua
//        List<Book> paidBooks = bookRepository.findBooksPaidByUserId(userId);
//
//        Set<Long> paidBookIds = paidBooks.stream()
//                .map(Book::getId)
//                .collect(Collectors.toSet());
//
//        if (paidBookIds.isEmpty()) {
//            return ApiResponse.success("Suggest books from friends", pageResult);
//        }
//
//        // 2. Lọc danh sách gợi ý từ bạn bè (loại bỏ sách đã mua)
//        List<SuggestBooksFromFriendDTO> filteredList = pageResult.getContent().stream()
//                .filter(dto -> !paidBookIds.contains(dto.id()))
//                .collect(Collectors.toList());
//
//        Page<SuggestBooksFromFriendDTO> mergedPage = new PageImpl<>(
//                filteredList,
//                pageResult.getPageable(),
//                pageResult.getTotalElements() - paidBookIds.size());

        return ApiResponse.success("Suggest books from friends", pageResult);
    }

    public ApiResponse<List<TopBookDTO>> getTopBooks() {
        List<TopBookDTO> topBookDTOS = bookRepository.findTop5SellingBooks(PageRequest.of(0, 5));
        return ApiResponse.success("Top books", topBookDTOS);
    }

    public ApiResponse<List<TopBookDTO>> getTopNewBooks() {
        List<TopBookDTO> topBookDTOS = bookRepository.findTop5NewBooks(PageRequest.of(0, 5));
        return ApiResponse.success("Top books", topBookDTOS);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<BookStockMax50DTO>> getBooksStockMax50(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("stock").descending());

        Page<BookStockMax50DTO> books = bookRepository.findBooksByStockMax50(pageable);


        return ApiResponse.success("List books of stock max 50", books);
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

        BigDecimal oldSalePrice = book.getSalePrice();
        BigDecimal newSalePrice = bookDTO.getSalePrice();

        // Cập nhật thông tin
        book.setTitle(bookDTO.getTitle());
        book.setAuthor(bookDTO.getAuthor());
        book.setPrice(bookDTO.getPrice());
        book.setSalePrice(newSalePrice);  // <-- set sau khi lấy oldSalePrice
        book.setStock(bookDTO.getStock());
        book.setDescription(bookDTO.getDescription());
        book.setImgUrl(bookDTO.getImgUrl());
        book.setCategory(category);

        Book savedBook = bookRepository.save(book);

        // Kiểm tra xem có phải từ "không giảm giá" → "giảm giá"
        boolean oldIsZero = oldSalePrice == null || oldSalePrice.compareTo(BigDecimal.ZERO) == 0;
        boolean newIsDiscounted = newSalePrice != null && newSalePrice.compareTo(BigDecimal.ZERO) > 0;

        if (oldIsZero && newIsDiscounted) {
            List<BookMark> bookMarkList = bookMarkRepository.findByBookId(book.getId());
            for (BookMark bm : bookMarkList) {
                notificationService.createBookMarkDiscountNotification(bm.getUser().getId(), book);
            }
        }

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
