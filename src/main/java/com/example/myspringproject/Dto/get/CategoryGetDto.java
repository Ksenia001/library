// file: src/main/java/com/example/myspringproject/Dto/CategoryGetDto.java
package com.example.myspringproject.Dto.get;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import com.example.myspringproject.model.Category;
import com.example.myspringproject.model.Book;

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
