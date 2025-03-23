package com.example.myspringproject.service;

import com.example.myspringproject.dto.create.BookCreateDto;
import com.example.myspringproject.dto.update.BookUpdateDto;
import com.example.myspringproject.model.Book;
import java.util.List;

public interface BookService {

    List<Book> findAllBooks();

    Book createBook(BookCreateDto dto);

    Book findBookById(int id);

    Book updateBook(BookUpdateDto dto);

    void deleteBookById(int id);

    List<Book> findBooksByAuthor(String author);

    List<Book> findBooksByName(String title);

    List<Book> searchBooks(String author, String title);
}
