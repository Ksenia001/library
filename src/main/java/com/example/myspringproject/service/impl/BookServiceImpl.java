package com.example.myspringproject.service.impl;

import com.example.myspringproject.Dto.create.BookCreateDto;
import com.example.myspringproject.Dto.update.BookUpdateDto;
import com.example.myspringproject.model.Book;
import com.example.myspringproject.model.Category;
import com.example.myspringproject.repository.BookRepository;
import com.example.myspringproject.repository.CategoryRepository;
import com.example.myspringproject.service.BookService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Primary
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public List<Book> findAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    public Book findBookById(int id) {
        return bookRepository.findById(id).orElse(null);
    }

    @Override
    public Book createBook(BookCreateDto dto) {
        Book book = new Book();
        book.setBookName(dto.getBookName());
        book.setBookAuthor(dto.getBookAuthor());

        if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(dto.getCategoryIds());

            // Проверка, что все категории найдены
            if (categories.size() != dto.getCategoryIds().size()) {
                List<Integer> foundCategoryIds = categories.stream()
                        .map(Category::getCategoryId)
                        .toList();
                List<Integer> missingCategoryIds = dto.getCategoryIds().stream()
                        .filter(id -> !foundCategoryIds.contains(id))
                        .toList();

                throw new IllegalArgumentException("Categories not found with IDs: " + missingCategoryIds);
            }

            book.setCategories(categories);
            categories.forEach(cat -> cat.getBooks().add(book));
        }

        return bookRepository.save(book);
    }

    @Override
    public Book updateBook(BookUpdateDto dto) {
        Book book = bookRepository.findById(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("Book not found"));

        book.setBookName(dto.getBookName());
        book.setBookAuthor(dto.getBookAuthor());

        if (dto.getCategoriesIds() != null) {
            // Удаляем старые связи
            book.getCategories().forEach(cat -> cat.getBooks().remove(book));
            book.getCategories().clear();

            // Добавляем новые
            List<Category> categories = categoryRepository.findAllById(dto.getCategoriesIds());
            if (categories.size() != dto.getCategoriesIds().size()) {
                throw new IllegalArgumentException("Some categories not found");
            }
            book.setCategories(categories);
            categories.forEach(cat -> cat.getBooks().add(book));
        }

        return bookRepository.save(book);
    }

    @Override
    public void deleteBookById(int id) {
        bookRepository.deleteById(id);
    }

    @Override
    public List<Book> findBooksByAuthor(String author) {
        return bookRepository.findByBookAuthorContainingIgnoreCase(author);
    }

    @Override
    public List<Book> findBooksByName(String title) {
        return bookRepository.findByBookNameContainingIgnoreCase(title);
    }

    @Override
    public List<Book> searchBooks(String author, String title) {
        return bookRepository.findByBookAuthorContainingIgnoreCaseOrBookNameContainingIgnoreCase(author, title);
    }
}

