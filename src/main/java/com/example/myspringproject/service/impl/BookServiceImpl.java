package com.example.myspringproject.service.impl;

import com.example.myspringproject.model.Book;
import com.example.myspringproject.repository.BookRepository;
import com.example.myspringproject.service.BookService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Primary
public class BookServiceImpl implements BookService {
    private final BookRepository repository;

    @Override
    public List<Book> findAllBooks() {
        return repository.findAll();
    }

    @Override
    public void saveBook(Book book) {
        repository.save(book);
    }

    @Override
    public Book findBookById(int id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public Book updateBook(Book book) {
        return repository.save(book);
    }

    @Override
    public void deleteBookById(int id) {
        repository.deleteById(id);
    }

    @Override
    public List<Book> findBooksByAuthor(String author) {
        return repository.findByBookAuthorContainingIgnoreCase(author);
    }

    @Override
    public List<Book> findBooksByName(String title) {
        return repository.findByBookNameContainingIgnoreCase(title);
    }
}

