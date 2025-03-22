package com.example.myspringproject.service.impl;

import com.example.myspringproject.Dto.create.CategoryCreateDto;
import com.example.myspringproject.Dto.update.CategoryUpdateDto;
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

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category getCategoryById(int id) {
        return categoryRepository.findById(id).orElse(null);
    }

    @Override
    public Category createCategory(CategoryCreateDto dto) {
        Category category = new Category();
        category.setCategoryName(dto.getName());

        if (dto.getBookIds() != null) {
            List<Book> books = bookRepository.findAllById(dto.getBookIds());
            // Проверяем, что все книги найдены
            if (books.size() != dto.getBookIds().size()) {
                throw new IllegalArgumentException("Some books not found");
            }
            category.setBooks(books);
            books.forEach(book -> book.getCategories().add(category));
        }

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
            // Заменяем весь список книг
            category.setBooks(books);
            // Обновляем обратную связь
            books.forEach(book -> {
                if (!book.getCategories().contains(category)) {
                    book.getCategories().add(category);
                }
            });
        }

        return categoryRepository.save(category);
    }

    @Transactional
    @Override
    public void deleteCategory(int id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        // Удаляем категорию из всех связанных книг
        category.getBooks().forEach(book ->
            book.getCategories().removeIf(c -> c.getCategoryId() == id)
        );

        categoryRepository.delete(category);
    }

    @Override
    public List<Category> findCategoriesByName(String name) {
        return categoryRepository.findByCategoryNameContainingIgnoreCase(name);
    }

}
