package com.example.myspringproject.dto.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookUpdateDto {
    @NotBlank(message = "Название книги не может быть пустым")
    @Size(min = 1, max = 20, message = "Название книги должно быть длиной от 1 до 20 символов")
    private String bookName;

    @Positive(message = "ID автора должен быть положительным")
    private Integer authorId;

    private List<@Positive(message = "ID категории должен быть положительным") Integer>
            categoriesIds;
}
