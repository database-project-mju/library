package mju.library.domain.book.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mju.library.domain.book.Book;

import java.util.List;

@Getter
@AllArgsConstructor
public class MainPageBooksDto {
    private final List<Book> recentBooks;
    private final List<Book> popularByLikes;
    private final List<Book> popularByLending;
}
