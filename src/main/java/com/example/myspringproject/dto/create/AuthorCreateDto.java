package com.example.myspringproject.dto.create;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthorCreateDto {
    @NotBlank
    private String authorName;
}