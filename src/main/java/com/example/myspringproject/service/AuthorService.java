package com.example.myspringproject.service;

import com.example.myspringproject.dto.create.AuthorCreateDto;
import com.example.myspringproject.dto.update.AuthorUpdateDto;
import com.example.myspringproject.model.Author;
import java.util.List;

public interface AuthorService {
    List<Author> findAllAuthors();

    Author findAuthorById(int id);

    Author createAuthor(AuthorCreateDto dto);

    Author updateAuthor(int id, AuthorUpdateDto dto);

    void deleteAuthor(int id);

    List<Author> findAuthorsByName(String name);

    List<Author> findAuthorsByBookCategory(String category);

    List<Author> findAuthorsByBookCategoryNative(String category);
}
