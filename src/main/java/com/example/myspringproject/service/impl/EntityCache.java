package com.example.myspringproject.service.impl;

import com.example.myspringproject.model.Author;
import com.example.myspringproject.model.Book;
import com.example.myspringproject.model.Category;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;


@Component
public class EntityCache {

    // Кэш для отдельных сущностей
    private final Map<Integer, Author> authorCache = new HashMap<>();
    private final Map<Integer, Book> bookCache = new HashMap<>();
    private final Map<Integer, Category> categoryCache = new HashMap<>();

    // Кэш для списков всех сущностей
    private List<Author> allAuthorsCache;
    private List<Book> allBooksCache;
    private List<Category> allCategoriesCache;

    // Методы для работы с авторами
    public Author getAuthor(int id) {
        return authorCache.get(id);
    }

    public void putAuthor(int id, Author author) {
        authorCache.put(id, author);
    }

    public void removeAuthor(int id) {
        authorCache.remove(id);
    }

    public List<Author> getAllAuthors() {
        return allAuthorsCache;
    }

    public void setAllAuthors(List<Author> authors) {
        this.allAuthorsCache = authors;
    }

    public void invalidateAllAuthors() {
        this.allAuthorsCache = null;
    }

    // Методы для работы с книгами
    public Book getBook(int id) {
        return bookCache.get(id);
    }

    public void putBook(int id, Book book) {
        bookCache.put(id, book);
    }

    public void removeBook(int id) {
        bookCache.remove(id);
    }

    public List<Book> getAllBooks() {
        return allBooksCache;
    }

    public void setAllBooks(List<Book> books) {
        this.allBooksCache = books;
    }

    public void invalidateAllBooks() {
        this.allBooksCache = null;
    }

    // Методы для работы с категориями
    public Category getCategory(int id) {
        return categoryCache.get(id);
    }

    public void putCategory(int id, Category category) {
        categoryCache.put(id, category);
    }

    public void removeCategory(int id) {
        categoryCache.remove(id);
    }

    public List<Category> getAllCategories() {
        return allCategoriesCache;
    }

    public void setAllCategories(List<Category> categories) {
        this.allCategoriesCache = categories;
    }

    public void invalidateAllCategories() {
        this.allCategoriesCache = null;
    }
}
