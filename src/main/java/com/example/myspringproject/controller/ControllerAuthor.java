package com.example.myspringproject.controller;

import com.example.myspringproject.dto.create.AuthorCreateDto;
import com.example.myspringproject.dto.get.AuthorGetDto;
import com.example.myspringproject.dto.update.AuthorUpdateDto;
import com.example.myspringproject.model.Author;
import com.example.myspringproject.service.AuthorService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/api/v2/authors")
@AllArgsConstructor
public class ControllerAuthor {

    private final AuthorService authorService;

    @GetMapping
    public ResponseEntity<List<AuthorGetDto>> findAllAuthors() {
        List<Author> authors = authorService.findAllAuthors();
        List<AuthorGetDto> dtos = authors.stream().map(AuthorGetDto::new).toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthorGetDto> findAuthorById(@PathVariable int id) {
        Author author = authorService.findAuthorById(id);
        return ResponseEntity.ok(new AuthorGetDto(author));
    }

    @PostMapping
    public ResponseEntity<AuthorGetDto> createAuthor(@RequestBody @Valid AuthorCreateDto dto) {
        Author createdAuthor = authorService.createAuthor(dto);
        return ResponseEntity.ok(new AuthorGetDto(createdAuthor));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuthorGetDto> updateAuthor(
            @PathVariable int id,
            @RequestBody @Valid AuthorUpdateDto dto
    ) {
        Author updatedAuthor = authorService.updateAuthor(id, dto);
        return ResponseEntity.ok(new AuthorGetDto(updatedAuthor));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable int id) {
        authorService.deleteAuthor(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<AuthorGetDto>> searchAuthors(
            @RequestParam("name") String name
    ) {
        List<Author> authors = authorService.findAuthorsByName(name);
        List<AuthorGetDto> dtos = authors.stream().map(AuthorGetDto::new).toList();
        return ResponseEntity.ok(dtos);
    }
}
