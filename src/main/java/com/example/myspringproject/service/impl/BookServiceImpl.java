package com.example.myspringproject.service.impl;

import com.example.myspringproject.cache.BookCache;
import com.example.myspringproject.cache.CategoryCache;
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
    private final BookCache bookCache;
    private final CategoryCache categoryCache;

    @Override
    public List<Book> findAllBooks() {
        return bookRepository.findAllWithCategoriesAndAuthor();
    }

    @Override
    public Book findBookById(int id) {
        String cacheKey = "book_id_" + id;
        if (bookCache.containsKey(cacheKey)) {
            return bookCache.get(cacheKey).stream()
                    .filter(book -> book.getBookId() == id)
                    .findFirst()
                    .orElse(null);
        }
        Book book = bookRepository.findById(id).orElse(null);
        if (book != null) {
            bookCache.put(cacheKey, List.of(book));
        }
        return book;
    }

    @Override
    public List<Book> searchBooks(String author, String title) {
        String cacheKey = "searchBooks_" + author + "_" + title;
        if (bookCache.containsKey(cacheKey)) {
            return bookCache.get(cacheKey);
        }
        List<Book> books = bookRepository
                .findByAuthorAuthorNameContainingIgnoreCaseOrBookNameContainingIgnoreCase(
                author, title);
        bookCache.put(cacheKey, books);
        return books;
    }

    @Override
    public List<Book> findBooksByCategory(String categoryName) {
        String cacheKey = "booksByCategory_" + categoryName;
        if (bookCache.containsKey(cacheKey)) {
            return bookCache.get(cacheKey);
        }
        List<Book> books = bookRepository.findByCategoryName(categoryName);
        bookCache.put(cacheKey, books);
        return books;
    }

    @Override
    public List<Book> findBooksByCategoryId(int categoryId) {
        String cacheKey = "booksByCategoryId_" + categoryId;
        if (bookCache.containsKey(cacheKey)) {
            return bookCache.get(cacheKey);
        }
        List<Book> books = bookRepository.findByCategoryId(categoryId);
        bookCache.put(cacheKey, books);
        return books;
    }

    public List<Book> findBooksByAuthor(String authorName)  {
        String cacheKey = "booksByAuthor_" + authorName;
        if (bookCache.containsKey(cacheKey)) {
            return bookCache.get(cacheKey);
        }
        List<Book> books = bookRepository.findByAuthorName(authorName);
        bookCache.put(cacheKey, books);
        return books;
    }

    @Override
    public List<Book> findBooksByAuthorId(int authorId) {
        String cacheKey = "booksByAuthorId_" + authorId;
        if (bookCache.containsKey(cacheKey)) {
            return bookCache.get(cacheKey);
        }
        List<Book> books = bookRepository.findByAuthorId(authorId);
        bookCache.put(cacheKey, books);
        return books;
    }

    @Override
    public Book createBook(BookCreateDto dto) {
        Book book = new Book();
        book.setBookName(dto.getName());

        Author author = authorRepository.findById(dto.getAuthorId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Author not found with ID: " + dto.getAuthorId()));
        book.setAuthor(author);
        author.getBooks().add(book);

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
        bookCache.clear();
        categoryCache.clear();
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
        bookCache.clear();
        categoryCache.clear();

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
        bookCache.clear();
        categoryCache.clear();
    }
}

