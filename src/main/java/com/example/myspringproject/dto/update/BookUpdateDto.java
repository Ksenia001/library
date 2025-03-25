package com.example.myspringproject.dto.update;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookUpdateDto {
    @NotNull
    private int id;

    private String bookName;

    @Positive
    private Integer authorId;

    private List<@Positive Integer> categoriesIds;
}
