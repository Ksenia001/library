package com.example.myspringproject.Dto.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookCreateDto {
    @NotBlank
    private String bookName;

    @NotBlank
    private String bookAuthor;

    @NotNull
    private List<@Positive Integer> categoryIds;
}
