package com.example.myspringproject.service.impl;

import com.example.myspringproject.dto.create.AuthorCreateDto;
import com.example.myspringproject.dto.update.AuthorUpdateDto;
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

    private static final String AUTHOR_NOT_FOUND_MESSAGE = "Author not found with ID: ";
    private final AuthorRepository authorRepository;

    @Override
    public List<Author> findAllAuthors() {
        return authorRepository.findAll();
    }

    @Override
    public Author findAuthorById(int id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(AUTHOR_NOT_FOUND_MESSAGE + id));
    }

    @Override
    public Author createAuthor(AuthorCreateDto dto) {
        Author author = new Author();
        author.setAuthorName(dto.getName());
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

        return authorRepository.save(author);
    }

    @Override
    @Transactional
    public void deleteAuthor(int id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(AUTHOR_NOT_FOUND_MESSAGE + id));

        // Remove the author reference from all associated books
        List<Book> books = author.getBooks();
        if (books != null) {
            books.forEach(book -> book.setAuthor(null));
        }

        authorRepository.delete(author);
    }

    @Override
    public List<Author> findAuthorsByName(String name) {
        return authorRepository.findByAuthorNameContainingIgnoreCase(name);
    }
}
