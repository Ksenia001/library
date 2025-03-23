package com.example.myspringproject.dto.update;

import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

// CategoryUpdateDto.java
@Getter
@Setter
public class CategoryUpdateDto {
    private String name;
    private List<@Positive Integer> bookIds;
}
