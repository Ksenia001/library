package com.example.MySpringProject.service.Impl;

import com.example.MySpringProject.model.Book;
import com.example.MySpringProject.repository.InMemoryBookDAO;
import com.example.MySpringProject.service.BookService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class InMemoryBookServiceImpl implements BookService {

    private final InMemoryBookDAO repository;

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
