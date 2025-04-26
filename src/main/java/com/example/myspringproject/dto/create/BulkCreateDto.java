package com.example.myspringproject.dto.create;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BulkCreateDto<T> {
    @NotNull(message = "Вы должны предоставить как минимум 1 книгу")
    @Size(min = 1, message = "Вы должны предоставить как минимум 1 книгу")
    private List<T> dtos;
}