package com.example.myspringproject.Dto.get;

import com.example.myspringproject.model.Category;
import lombok.Getter;
import lombok.Setter;
import com.example.myspringproject.model.Book;
import java.util.List;

@Getter
@Setter
public class BookGetDto {
    private int id;
    private String bookName;
    private List<String> categories;

    // Конвертация сущности Book в DTO
    public BookGetDto(Book book) {
        this.id = book.getBookId();
        this.bookName = book.getBookName();
        if (book.getCategories() != null) {
            this.categories = book.getCategories().stream()
                    .map(Category::getCategoryName)
                    .toList();
        }
    }
}
