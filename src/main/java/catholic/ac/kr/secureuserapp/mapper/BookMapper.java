package catholic.ac.kr.secureuserapp.mapper;

import catholic.ac.kr.secureuserapp.model.dto.BookDTO;
import catholic.ac.kr.secureuserapp.model.dto.CreateBookRequest;
import catholic.ac.kr.secureuserapp.model.entity.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "category.name",target = "categoryName")
    @Mapping(source = "category.id", target = "categoryId")
    BookDTO bookToBookDTO(Book book);

    @Mapping(target = "category", ignore = true) // map thủ công
    Book bookDTOToBook(BookDTO bookDTO);

    @Mapping(target = "category",ignore = true)
    @Mapping(target = "id",ignore = true)
    Book fromCreateBook(CreateBookRequest createBookRequest);

    List<BookDTO> toBookDTO(List<Book> books);
    List<Book> toBookList(List<BookDTO> bookDTOs);

    default Page<BookDTO> toBookDTO(Page<Book> books) {
        List<BookDTO> dtoList =toBookDTO(books.getContent());
        return new PageImpl<>(dtoList, books.getPageable(), books.getTotalElements());
    }
}
