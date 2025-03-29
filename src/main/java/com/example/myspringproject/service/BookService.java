package com.example.myspringproject.service;

import com.example.myspringproject.dto.create.BookCreateDto;
import com.example.myspringproject.dto.update.BookUpdateDto;
import com.example.myspringproject.model.Book;
import java.util.List;

public interface BookService {

    List<Book> findAllBooks();

    Book createBook(BookCreateDto dto);

    Book findBookById(int id);

    Book updateBook(int id, BookUpdateDto dto);

    void deleteBookById(int id);

    List<Book> searchBooks(String author, String title);

    List<Book> findBooksByCategory(String categoryName);

    List<Book> findBooksByCategoryId(int categoryId);

    List<Book> findBooksByAuthor(String authorName);

    List<Book> findBooksByAuthorId(int authorId);
}
