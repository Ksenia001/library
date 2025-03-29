package com.example.myspringproject.dto.create;

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
    private String name;

    @NotNull
    @Positive
    private Integer authorId;

    @NotNull
    private List<@Positive Integer> categoryIds;
}
