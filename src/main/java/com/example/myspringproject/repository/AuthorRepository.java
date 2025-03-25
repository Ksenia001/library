package com.example.myspringproject.repository;

import com.example.myspringproject.model.Author;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AuthorRepository extends JpaRepository<Author, Integer> {
    List<Author> findByAuthorNameContainingIgnoreCase(String authorName);
}
