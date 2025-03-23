package com.example.myspringproject.dto.update;

import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookUpdateDto {
    private int id;
    private String bookName;
    private String bookAuthor; // Добавлено
    private List<@Positive Integer> categoriesIds;
}
