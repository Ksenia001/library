package com.example.myspringproject.repository;

import com.example.myspringproject.model.Book;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Integer> {

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.categories LEFT JOIN FETCH b.author")
    List<Book> findAllWithCategoriesAndAuthor();

    List<Book> findByAuthorAuthorNameContainingIgnoreCaseOrBookNameContainingIgnoreCase(
            String authorName, String title
    );

    @Query("SELECT b FROM Book b JOIN b.categories c WHERE LOWER(c.categoryName) "
            + "LIKE LOWER(CONCAT('%', :categoryName, '%'))")
    List<Book> findByCategoryName(@Param("categoryName") String categoryName);

    @Query("SELECT b FROM Book b JOIN b.categories c WHERE c.categoryId = :categoryId")
    List<Book> findByCategoryId(@Param("categoryId") int categoryId);

    @Query("SELECT b FROM Book b JOIN b.author c WHERE LOWER(c.authorName) "
            + "LIKE LOWER(CONCAT('%', :authorName, '%'))")
    List<Book> findByAuthorName(@Param("authorName") String authorName);

    @Query("SELECT b FROM Book b WHERE b.author.authorId = :authorId")
    List<Book> findByAuthorId(@Param("authorId") int authorId);
}