package com.example.myspringproject.service.impl;

import com.example.myspringproject.cache.AuthorCache;
import com.example.myspringproject.dto.create.AuthorCreateDto;
import com.example.myspringproject.dto.update.AuthorUpdateDto;
import com.example.myspringproject.exception.UniqueConstraintViolationException;
import com.example.myspringproject.model.Author;
import com.example.myspringproject.model.Book;
import com.example.myspringproject.repository.AuthorRepository;
import com.example.myspringproject.service.AuthorService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthorServiceImpl implements AuthorService {

    private static final String AUTHOR_NOT_FOUND_MESSAGE = "Автор не найден с id: ";
    private final AuthorRepository authorRepository;
    private final AuthorCache authorCache;

    @Override
    public List<Author> findAllAuthors() {
        return authorRepository.findAllWithBooks();
    }

    @Override
    public Author findAuthorById(int id) {
        String cacheKey = "author_id_" + id;
        if (authorCache.containsKey(cacheKey)) {
            return authorCache.get(cacheKey).stream()
                    .filter(author -> author.getAuthorId() == id)
                    .findFirst()
                    .orElse(null);
        }
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(AUTHOR_NOT_FOUND_MESSAGE + id));
        authorCache.put(cacheKey, List.of(author));
        return author;
    }

    @Override
    public List<Author> findAuthorsByBookCategory(String category) {
        String cacheKey = "authorsByCategory_" + category;
        if (authorCache.containsKey(cacheKey)) {
            List<Author> cachedAuthors = authorCache.get(cacheKey);
            if (cachedAuthors.isEmpty()) {
                throw new EntityNotFoundException("Авторы не найдены по категории книги: "
                        + category);
            }
            return cachedAuthors;
        }
        List<Author> authors = authorRepository.findAuthorsByBookCategory(category);
        if (authors.isEmpty()) {
            throw new EntityNotFoundException("Авторы не найдены по категории книги: " + category);
        }
        authorCache.put(cacheKey, authors);
        return authors;
    }

    @Override
    public List<Author> findAuthorsByName(String name) {
        String cacheKey = "authorsByName_" + name;
        if (authorCache.containsKey(cacheKey)) {
            List<Author> cachedAuthors = authorCache.get(cacheKey);
            if (cachedAuthors.isEmpty()) {
                throw new EntityNotFoundException("Авторы не найдены по имени: " + name);
            }
            return cachedAuthors;
        }
        List<Author> authors = authorRepository.findByAuthorNameContainingIgnoreCase(name);
        if (authors.isEmpty()) {
            throw new EntityNotFoundException("Авторы не найдены по имени: " + name);
        }
        authorCache.put(cacheKey, authors);
        return authors;
    }

    @Override
    public List<Author> findAuthorsByBookCategoryNative(String category) {
        String cacheKey = "authorsByCategoryNative_" + category;
        if (authorCache.containsKey(cacheKey)) {
            return authorCache.get(cacheKey);
        }
        List<Author> authors = authorRepository.findAuthorsByBookCategoryNative(category);
        authorCache.put(cacheKey, authors);
        return authors;
    }

    @Override
    public Author createAuthor(AuthorCreateDto dto) {
        if (authorRepository.existsByAuthorName(dto.getName())) {
            throw new UniqueConstraintViolationException("Автор с таким именем уже существует");
        }
        Author author = new Author();
        author.setAuthorName(dto.getName());
        authorCache.clear();
        return authorRepository.save(author);
    }

    @Override
    @Transactional
    public Author updateAuthor(int id, AuthorUpdateDto dto) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(AUTHOR_NOT_FOUND_MESSAGE + id));

        if (dto.getAuthorName() != null && !dto.getAuthorName().isBlank()) {
            author.setAuthorName(dto.getAuthorName());
        }
        authorCache.clear();
        return authorRepository.save(author);
    }

    @Override
    @Transactional
    public void deleteAuthor(int id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(AUTHOR_NOT_FOUND_MESSAGE + id));

        List<Book> books = author.getBooks();
        if (books != null) {
            books.forEach(book -> book.setAuthor(null));
        }
        authorRepository.delete(author);
        authorCache.clear();
    }
}
