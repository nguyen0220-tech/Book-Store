package catholic.ac.kr.secureuserapp.mapper;

import catholic.ac.kr.secureuserapp.model.dto.BookMarkDTO;
import catholic.ac.kr.secureuserapp.model.entity.Book;
import catholic.ac.kr.secureuserapp.model.entity.BookMark;
import catholic.ac.kr.secureuserapp.model.entity.User;

public class BookMarkMapper {
    public static BookMarkDTO toBookMarkDTO(BookMark bookMark) {
        BookMarkDTO bookMarkDTO = new BookMarkDTO();
        bookMarkDTO.setId(bookMark.getId());
        bookMarkDTO.setUserId(bookMark.getUser().getId());
        bookMarkDTO.setBookId(bookMark.getBook().getId());
        bookMarkDTO.setTitle(bookMark.getBook().getTitle());
        bookMarkDTO.setAuthor(bookMark.getBook().getAuthor());
        bookMarkDTO.setDescription(bookMark.getBook().getDescription());
        bookMarkDTO.setPrice(bookMark.getBook().getPrice());
        bookMarkDTO.setSalePrice(bookMark.getBook().getSalePrice());
        bookMarkDTO.setImgUrl(bookMark.getBook().getImgUrl());

        return bookMarkDTO;
    }

    public static BookMark toBookMark(BookMarkDTO bookMarkDTO) {
        BookMark bookMark = new BookMark();

        bookMark.setId(bookMarkDTO.getId());

        User user = new User();
        user.setId(bookMarkDTO.getUserId());
        bookMark.setUser(user);

        Book book = new Book();
        book.setId(bookMarkDTO.getBookId());
        bookMark.setBook(book);

        return bookMark;

    }
}
