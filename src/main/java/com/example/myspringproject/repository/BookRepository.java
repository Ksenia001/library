package com.example.myspringproject.repository;

import com.example.myspringproject.model.Book;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Integer> {
    List<Book> findByAuthorAuthorNameContainingIgnoreCase(
            String authorName);

    List<Book> findByBookNameContainingIgnoreCase(String title);

    List<Book> findByAuthorAuthorNameContainingIgnoreCaseOrBookNameContainingIgnoreCase(
            String authorName, String title
    );
}