package com.example.myspringproject.service.impl;

import com.example.myspringproject.cache.CategoryCache;
import com.example.myspringproject.dto.create.CategoryCreateDto;
import com.example.myspringproject.dto.update.CategoryUpdateDto;
import com.example.myspringproject.model.Book;
import com.example.myspringproject.model.Category;
import com.example.myspringproject.repository.BookRepository;
import com.example.myspringproject.repository.CategoryRepository;
import com.example.myspringproject.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;
    private final CategoryCache categoryCache;

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category getCategoryById(int id) {
        String cacheKey = "category_id_" + id;
        if (categoryCache.containsKey(cacheKey)) {
            return categoryCache.get(cacheKey).stream()
                    .filter(category -> category.getCategoryId() == id)
                    .findFirst()
                    .orElse(null);
        }
        Category category = categoryRepository.findById(id).orElse(null);
        if (category != null) {
            categoryCache.put(cacheKey, List.of(category));
        }
        return category;
    }

    @Override
    public List<Category> findCategoriesByName(String name) {
        String cacheKey = "categoriesByName_" + name;
        if (categoryCache.containsKey(cacheKey)) {
            return categoryCache.get(cacheKey);
        }
        List<Category> categories = categoryRepository.findByCategoryNameContainingIgnoreCase(name);
        categoryCache.put(cacheKey, categories);
        return categories;
    }

    @Override
    public List<Category> findCategoriesByBook(String bookName) {
        String cacheKey = "categoriesByBook_" + bookName;
        if (categoryCache.containsKey(cacheKey)) {
            return categoryCache.get(cacheKey);
        }
        List<Category> categories = categoryRepository.findCategoriesByBook(bookName);
        categoryCache.put(cacheKey, categories);
        return categories;
    }

    @Override
    public List<Category> findCategoriesByBookId(int bookId) {
        String cacheKey = "categoriesByBookId_" + bookId;
        if (categoryCache.containsKey(cacheKey)) {
            return categoryCache.get(cacheKey);
        }
        List<Category> categories = categoryRepository.findCategoriesByBookId(bookId);
        categoryCache.put(cacheKey, categories);
        return categories;
    }

    @Override
    public Category createCategory(CategoryCreateDto dto) {
        Category category = new Category();
        category.setCategoryName(dto.getName());

        if (dto.getBookIds() != null) {
            List<Book> books = bookRepository.findAllById(dto.getBookIds());
            if (books.size() != dto.getBookIds().size()) {
                throw new IllegalArgumentException("Some books not found");
            }
            category.setBooks(books);
            books.forEach(book -> book.getCategories().add(category));
        }
        categoryCache.clear();

        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public Category updateCategory(int id, CategoryUpdateDto dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        category.setCategoryName(dto.getName());

        if (dto.getBookIds() != null) {
            List<Book> books = bookRepository.findAllById(dto.getBookIds());
            category.setBooks(books);
            books.forEach(book -> {
                if (!book.getCategories().contains(category)) {
                    book.getCategories().add(category);
                }
            });
        }
        categoryCache.clear();

        return categoryRepository.save(category);
    }

    @Transactional
    @Override
    public void deleteCategory(int id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        category.getBooks().forEach(book ->
            book.getCategories().removeIf(c -> c.getCategoryId() == id)
        );

        categoryRepository.delete(category);
        categoryCache.clear();

    }
}
