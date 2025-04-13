package com.example.myspringproject.dto.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthorCreateDto {
    @NotBlank(message = "Имя автора не может быть пустым")
    @Size(min = 1, max = 20, message = "Имя автора должно быть длиной от 1 до 20 символов")
    private String name;
}