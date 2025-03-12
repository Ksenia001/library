package com.example.myspringproject.service;

import com.example.myspringproject.model.Book;
import java.util.List;

public interface BookService {

    List<Book> findAllBooks();

    void saveBook(Book book);

    Book findBookById(int id);

    Book updateBook(Book book);

    void deleteBookById(int id);

    List<Book> findBooksByAuthor(String author);

    List<Book> findBooksByName(String bookName);
}
