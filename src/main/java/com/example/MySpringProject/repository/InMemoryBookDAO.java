package com.example.MySpringProject.repository;
import com.example.MySpringProject.model.Book;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

@Repository

public class InMemoryBookDAO {
    private final List<Book> BOOK = new ArrayList<>();

    public List<Book> findAllBooks() {
        return BOOK;
    }

    public Book saveBook(Book book) {
        BOOK.add(book);
        return book;
    }

    public Book findBookById(int id) {
        return BOOK.stream()
                .filter(book -> book.getBookISBN() == id)
                .findFirst()
                .orElse(null);
    }


    public Book updateBook(Book book) {
        int index = IntStream.range(0, BOOK.size())
                .filter(i -> BOOK.get(i).getBookISBN() == book.getBookISBN())
                .findFirst()
                .orElse(-1);
        if (index != -1) {
            BOOK.set(index, book);
            return book;
        }
        return null;
    }

    public void deleteBookById(int id) {
        var book = findBookById(id);
        if (book != null) {
            BOOK.remove(book);
        }
    }

    public List<Book> findBooksByAuthor(String author) {
        return BOOK.stream()
                .filter(book -> book.getBookAuthor() != null &&
                        book.getBookAuthor().toLowerCase().contains(author.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Book> findBooksByName(String bookName) {
        return BOOK.stream()
                .filter(book -> book.getBookName() != null &&
                        book.getBookName().toLowerCase().contains(bookName.toLowerCase()))
                .collect(Collectors.toList());
    }
}
