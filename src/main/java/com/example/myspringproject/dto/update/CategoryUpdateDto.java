package com.example.myspringproject.dto.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryUpdateDto {
    @NotBlank(message = "Название категории не может быть пустым")
    @Size(min = 1, max = 40, message = "Название категории должно быть длиной от 1 до 40 символов")

    private String name;

    private List<@Positive(message = "ID книги должно быть положительным") Integer> bookIds;
}
