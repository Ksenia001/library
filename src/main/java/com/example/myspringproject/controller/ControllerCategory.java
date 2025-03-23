package com.example.myspringproject.controller;

import com.example.myspringproject.dto.create.CategoryCreateDto;
import com.example.myspringproject.dto.get.CategoryGetDto;
import com.example.myspringproject.dto.update.CategoryUpdateDto;
import com.example.myspringproject.model.Category;
import com.example.myspringproject.service.CategoryService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/categories")
@AllArgsConstructor
public class ControllerCategory {
    private final CategoryService categoryService;

    // Получение списка всех категорий
    @GetMapping
    public ResponseEntity<List<CategoryGetDto>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        List<CategoryGetDto> dtos = categories.stream()
                .map(CategoryGetDto::new)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    // Получение категории по идентификатору
    @GetMapping("/{id}")
    public ResponseEntity<CategoryGetDto> getCategoryById(@PathVariable int id) {
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(new CategoryGetDto(category));
    }

    // Создание новой категории
    @PostMapping
    public ResponseEntity<CategoryGetDto> createCategory(
            @RequestBody @Valid CategoryCreateDto dto
    ) {
        Category createdCategory = categoryService.createCategory(dto);
        return ResponseEntity.ok(new CategoryGetDto(createdCategory));
    }

    // Обновление существующей категории
    @PutMapping("/{id}")
    public ResponseEntity<CategoryGetDto> updateCategory(
            @PathVariable int id,
            @RequestBody @Valid CategoryUpdateDto dto
    ) {
        Category updatedCategory = categoryService.updateCategory(id, dto);
        return ResponseEntity.ok(new CategoryGetDto(updatedCategory));
    }

    // Удаление категории
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable int id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<CategoryGetDto>> searchCategories(
            @RequestParam("name") String name) {
        List<Category> categories = categoryService.findCategoriesByName(name);
        List<CategoryGetDto> dtos = categories.stream()
                .map(CategoryGetDto::new)
                .toList();
        return ResponseEntity.ok(dtos);
    }

}
