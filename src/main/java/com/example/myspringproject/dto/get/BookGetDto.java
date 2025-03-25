package com.example.myspringproject.dto.get;

import com.example.myspringproject.model.Book;
import com.example.myspringproject.model.Category;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookGetDto {
    private int id;
    private String bookName;
    private String authorName;
    private List<String> categories;

    public BookGetDto(Book book) {
        this.id = book.getBookId();
        this.bookName = book.getBookName();
        if (book.getAuthor() != null) {
            this.authorName = book.getAuthor().getAuthorName();
        }
        if (book.getCategories() != null) {
            this.categories = book.getCategories().stream()
                    .map(Category::getCategoryName)
                    .toList();
        }
    }
}
