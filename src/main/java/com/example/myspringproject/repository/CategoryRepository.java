package com.example.myspringproject.repository;

import com.example.myspringproject.model.Category;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findByCategoryNameContainingIgnoreCase(String categoryName);

    @Query("SELECT c FROM Category c JOIN c.books b WHERE LOWER(b.bookName) "
            + "LIKE LOWER(CONCAT('%', :bookName, '%'))")
    List<Category> findCategoriesByBook(@Param("bookName") String bookName);

    @Query("SELECT c FROM Category c JOIN c.books b WHERE b.bookId = :bookId")
    List<Category> findCategoriesByBookId(@Param("bookId") int bookId);

    boolean existsByCategoryName(String name);

    List<Category> findByCategoryNameIn(@Param("names") Set<String> names);

}
