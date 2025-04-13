package com.example.myspringproject.controller;

import com.example.myspringproject.dto.create.CategoryCreateDto;
import com.example.myspringproject.dto.get.CategoryGetDto;
import com.example.myspringproject.dto.update.CategoryUpdateDto;
import com.example.myspringproject.model.Category;
import com.example.myspringproject.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Categories", description = "API for managing categories")
public class ControllerCategory {
    private final CategoryService categoryService;

    // Получение списка всех категорий
    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieve a list of all categories")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CategoryGetDto>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        List<CategoryGetDto> dtos = categories.stream()
                .map(CategoryGetDto::new)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    // Получение категории по идентификатору
    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieve a category by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category found"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @Parameter(description = "ID of the category to retrieve", name = "id")
    public ResponseEntity<CategoryGetDto> getCategoryById(@PathVariable int id) {
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(new CategoryGetDto(category));
    }

    // Создание новой категории
    @PostMapping
    @Operation(summary = "Create a new category",
            description = "Create a new category with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category created"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<CategoryGetDto> createCategory(
        @RequestBody @Valid CategoryCreateDto dto
    ) {
        Category createdCategory = categoryService.createCategory(dto);
        return ResponseEntity.ok(new CategoryGetDto(createdCategory));
    }

    // Обновление существующей категории
    @PutMapping("/{id}")
    @Operation(summary = "Update a category", description = "Update an existing category by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category updated"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @Parameter(description = "ID of the category to update", name = "id")
    public ResponseEntity<CategoryGetDto> updateCategory(
        @PathVariable int id,
        @RequestBody @Valid CategoryUpdateDto dto
    ) {
        Category updatedCategory = categoryService.updateCategory(id, dto);
        return ResponseEntity.ok(new CategoryGetDto(updatedCategory));
    }

    @Operation(summary = "Delete a category", description = "Delete a category by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Category deleted"),
        @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @Parameter(description = "ID of the category to delete", name = "id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable int id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Search categories by name",
            description = "Search for categories by their name")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation"),
        @ApiResponse(responseCode = "404", description = "Categories not found")
    })
    @Parameter(description = "Name or part of the name to search for", name = "name")
    @GetMapping("/search")
    public ResponseEntity<List<CategoryGetDto>> searchCategories(
        @RequestParam("name") String name) {
        List<Category> categories = categoryService.findCategoriesByName(name);
        List<CategoryGetDto> dtos = categories.stream()
                .map(CategoryGetDto::new)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Get categories by book name",
            description = "Retrieve categories associated with a specific book by name")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation")
    })
    @Parameter(description = "Book name to filter categories by", name = "book")
    @GetMapping("/by-book")
    public ResponseEntity<List<CategoryGetDto>> getCategoriesByBook(
        @RequestParam("book") String bookName
    ) {
        List<Category> categories = categoryService.findCategoriesByBook(bookName);
        List<CategoryGetDto> dtos = categories.stream()
                .map(CategoryGetDto::new)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Get categories by book ID",
            description = "Retrieve categories associated with a specific book by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation")
    })
    @Parameter(description = "ID of the book to filter categories by", name = "bookId")
    @GetMapping("/by-book/{bookId}")
    public ResponseEntity<List<CategoryGetDto>> getCategoriesByBookId(
        @PathVariable int bookId
    ) {
        List<Category> categories = categoryService.findCategoriesByBookId(bookId);
        List<CategoryGetDto> dtos = categories.stream()
                .map(CategoryGetDto::new)
                .toList();
        return ResponseEntity.ok(dtos);
    }
}
