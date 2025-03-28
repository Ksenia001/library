package com.example.myspringproject.service.impl;

import com.example.myspringproject.dto.create.BookCreateDto;
import com.example.myspringproject.dto.update.BookUpdateDto;
import com.example.myspringproject.model.Author;
import com.example.myspringproject.model.Book;
import com.example.myspringproject.model.Category;
import com.example.myspringproject.repository.AuthorRepository;
import com.example.myspringproject.repository.BookRepository;
import com.example.myspringproject.repository.CategoryRepository;
import com.example.myspringproject.service.BookService;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
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
    private final AuthorRepository authorRepository;

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
        book.setBookName(dto.getName());

        // Set the Author
        Author author = authorRepository.findById(dto.getAuthorId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Author not found with ID: " + dto.getAuthorId()));
        book.setAuthor(author);
        author.getBooks().add(book);

        // Set Categories
        if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(dto.getCategoryIds());
            if (categories.size() != dto.getCategoryIds().size()) {
                throw new IllegalArgumentException("Some categories not found");
            }

            categories.forEach(cat -> {
                if (cat.getBooks() == null) {
                    cat.setBooks(new ArrayList<>());
                }
                cat.getBooks().add(book);
            });
            book.setCategories(categories);
        }

        return bookRepository.save(book);
    }

    @Override
    public Book updateBook(int id, BookUpdateDto dto) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book not found"));

        book.setBookName(dto.getBookName());

        if (dto.getAuthorId() != null) {
            Author author = authorRepository.findById(dto.getAuthorId())
                    .orElseThrow(() -> new EntityNotFoundException("Author not found"));
            book.setAuthor(author);
        }

        if (dto.getCategoriesIds() != null) {
            List<Category> categories = categoryRepository.findAllById(dto.getCategoriesIds());
            book.setCategories(categories);
        }

        return bookRepository.save(book);
    }

    @Override
    public void deleteBookById(int id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book not found"));
        if (book.getAuthor() != null) {
            book.getAuthor().getBooks().remove(book);
        }
        bookRepository.deleteById(id);
    }

    @Override
    public List<Book> searchBooks(String author, String title) {
        return
                bookRepository
                        .findByAuthorAuthorNameContainingIgnoreCaseOrBookNameContainingIgnoreCase(
                        author, title);
    }

    @Override
    public List<Book> findBooksByCategory(String categoryName) {
        return bookRepository.findByCategoryName(categoryName);
    }

    @Override
    public List<Book> findBooksByCategoryId(int categoryId) {
        return bookRepository.findByCategoryId(categoryId);
    }

    public List<Book> findBooksByAuthor(String authorName)  {
        return bookRepository.findByAuthorName(authorName);
    }

    @Override
    public List<Book> findBooksByAuthorId(int authorId) {
        return bookRepository.findByAuthorId(authorId);
    }
}

