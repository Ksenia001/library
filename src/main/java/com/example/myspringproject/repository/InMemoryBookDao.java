package com.example.myspringproject.repository;

import com.example.myspringproject.model.Book;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.stereotype.Repository;

@Repository

public class InMemoryBookDao {
    private final List<Book> bookArrayList = new ArrayList<>();

    public List<Book> findAllBooks() {
        return bookArrayList;
    }

    public void saveBook(Book book) {
        bookArrayList.add(book);
    }

    public Book findBookById(int id) {
        return bookArrayList.stream()
                .filter(book -> book.getBookId() == id)
                .findFirst()
                .orElse(null);
    }


    public Book updateBook(Book book) {
        int index = IntStream.range(0, bookArrayList.size())
                .filter(i -> bookArrayList.get(i).getBookId() == book.getBookId())
                .findFirst()
                .orElse(-1);
        if (index != -1) {
            bookArrayList.set(index, book);
            return book;
        }
        return null;
    }

    public void deleteBookById(int id) {
        var book = findBookById(id);
        if (book != null) {
            bookArrayList.remove(book);
        }
    }

    public List<Book> findBooksByAuthor(String author) {
        return bookArrayList.stream()
                .filter(book -> book.getBookAuthor() != null
                        &&
                        book.getBookAuthor().toLowerCase().contains(author.toLowerCase()))
                .toList();
    }

    public List<Book> findBooksByName(String bookName) {
        return bookArrayList.stream()
                .filter(book -> book.getBookName() != null
                        &&
                        book.getBookName().toLowerCase().contains(bookName.toLowerCase()))
                .toList();
    }
}
