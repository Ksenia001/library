package com.example.myspringproject.service;

import com.example.myspringproject.Dto.create.CategoryCreateDto;
import com.example.myspringproject.Dto.update.CategoryUpdateDto;
import com.example.myspringproject.model.Category;
import java.util.List;

public interface CategoryService {

    List<Category> getAllCategories();

    Category getCategoryById(int id);

    Category createCategory(CategoryCreateDto dto);

    Category updateCategory(int id, CategoryUpdateDto dto);

    void deleteCategory(int id);

    List<Category> findCategoriesByName(String name);
}

