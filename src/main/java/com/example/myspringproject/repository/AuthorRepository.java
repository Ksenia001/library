package com.example.myspringproject.repository;

import com.example.myspringproject.model.Author;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface AuthorRepository extends JpaRepository<Author, Integer> {
    List<Author> findByAuthorNameContainingIgnoreCase(String authorName);

    @Query("SELECT DISTINCT a FROM Author a JOIN a.books b JOIN b.categories c WHERE "
            + "LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :category, '%'))")
    List<Author> findAuthorsByBookCategory(@Param("category") String category);
}
