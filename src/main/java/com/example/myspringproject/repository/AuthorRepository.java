package com.example.myspringproject.repository;

import com.example.myspringproject.model.Author;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface AuthorRepository extends JpaRepository<Author, Integer> {
    @Query("SELECT DISTINCT a FROM Author a LEFT JOIN FETCH a.books")
    List<Author> findAllWithBooks();

    // Загрузка авторов по имени с книгами
    @EntityGraph(attributePaths = {"books"})
    List<Author> findByAuthorNameContainingIgnoreCase(String authorName);

    @Query("SELECT DISTINCT a FROM Author a JOIN a.books b JOIN b.categories c WHERE "
            + "LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :category, '%'))")
    List<Author> findAuthorsByBookCategory(@Param("category") String category);

    @Query(value = """
         SELECT DISTINCT a.*
         FROM authors a
         JOIN books b ON a.author_id = b.author_id
         JOIN books_categories bc ON b.book_id = bc.book_id
         JOIN categories c ON bc.category_id = c.category_id
         WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :category, '%'))
         """,
            nativeQuery = true)
    List<Author> findAuthorsByBookCategoryNative(@Param("category") String category);
}
