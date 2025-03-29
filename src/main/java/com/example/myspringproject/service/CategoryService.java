package com.example.myspringproject.service;

import com.example.myspringproject.dto.create.CategoryCreateDto;
import com.example.myspringproject.dto.update.CategoryUpdateDto;
import com.example.myspringproject.model.Category;
import java.util.List;

public interface CategoryService {

    List<Category> getAllCategories();

    Category getCategoryById(int id);

    Category createCategory(CategoryCreateDto dto);

    Category updateCategory(int id, CategoryUpdateDto dto);

    void deleteCategory(int id);

    List<Category> findCategoriesByName(String name);

    List<Category> findCategoriesByBook(String bookName);

    List<Category> findCategoriesByBookId(int bookId);
}

