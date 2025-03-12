package com.example.myspringproject.service.impl;

import com.example.myspringproject.model.Book;
import com.example.myspringproject.repository.InMemoryBookDao;
import com.example.myspringproject.service.BookService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class InMemoryBookServiceImpl implements BookService {

    private final InMemoryBookDao repository;

    @Override
    public List<Book> findAllBooks() {
        return repository.findAllBooks();
    }

    @Override
    public void saveBook(Book book) {
        repository.saveBook(book);
    }

    @Override
    public Book findBookById(int id) {
        return repository.findBookById(id);
    }

    @Override
    public Book updateBook(Book book) {
        return repository.updateBook(book);
    }

    @Override
    public void deleteBookById(int id) {
        repository.deleteBookById(id);
    }

    @Override
    public List<Book> findBooksByAuthor(String author) {
        return repository.findBooksByAuthor(author);
    }

    @Override
    public List<Book> findBooksByName(String bookName) {
        return repository.findBooksByName(bookName);
    }
}
