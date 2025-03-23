package com.example.myspringproject.dto.get;

import com.example.myspringproject.model.Book;
import com.example.myspringproject.model.Category;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryGetDto {
    private int id;
    private String name;
    private List<String> books;

    // Конструктор для преобразования сущности Category в DTO
    public CategoryGetDto(Category category) {
        this.id = category.getCategoryId();
        this.name = category.getCategoryName();
        if (category.getBooks() != null) {
            this.books = category.getBooks().stream()
                    .map(Book::getBookName)
                    .toList();
        }
    }
}
