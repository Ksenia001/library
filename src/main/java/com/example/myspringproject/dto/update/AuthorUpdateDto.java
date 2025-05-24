package com.example.myspringproject.dto.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthorUpdateDto {
    @NotBlank(message = "Имя автора не может быть пустым")
    @Size(min = 1, max = 40, message = "Имя автора должно быть длиной от 1 до 40 символов")
    private String authorName;
}
