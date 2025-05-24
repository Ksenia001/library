package com.example.myspringproject.dto.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookCreateDto {
    @NotBlank(message = "Название книги не может быть пустым")
    @Size(min = 1, max = 40, message = "Название книги должно быть длиной от 1 до 20 символов")
    private String name;

    @Positive(message = "ID автора должен быть положительным")
    private Integer authorId;

    private String authorName;

    private List<@Positive(message = "ID категории должен быть положительным") Integer> categoryIds;

    private List<String> categoryNames;
}
