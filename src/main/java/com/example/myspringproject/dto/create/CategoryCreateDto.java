package com.example.myspringproject.dto.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryCreateDto {
    @NotBlank(message = "Название категории не может быть пустым")
    @Size(min = 1, max = 40, message = "Название категории должно быть длиной от 1 до 20 символов")

    private String name;

    private List<@Positive@Positive(message = "ID книги должно быть положительным") Integer>
            bookIds;
}

