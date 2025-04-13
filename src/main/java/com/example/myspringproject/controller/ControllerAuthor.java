package com.example.myspringproject.controller;

import com.example.myspringproject.dto.create.AuthorCreateDto;
import com.example.myspringproject.dto.get.AuthorGetDto;
import com.example.myspringproject.dto.update.AuthorUpdateDto;
import com.example.myspringproject.model.Author;
import com.example.myspringproject.service.AuthorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authors", description = "API for managing authors")
public class ControllerAuthor {

    private final AuthorService authorService;

    @GetMapping
    @Operation(summary = "Get all authors", description = "Retrieve a list of all authors")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<AuthorGetDto>> findAllAuthors() {
        List<Author> authors = authorService.findAllAuthors();
        List<AuthorGetDto> dtos = authors.stream().map(AuthorGetDto::new).toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get author by ID", description = "Retrieve an author by their ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Author found"),
        @ApiResponse(responseCode = "404", description = "Author not found")
    })
    public ResponseEntity<AuthorGetDto> findAuthorById(@PathVariable int id) {
        Author author = authorService.findAuthorById(id);
        return ResponseEntity.ok(new AuthorGetDto(author));
    }

    @PostMapping
    @Operation(summary = "Create a new author",
            description = "Create a new author with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Author created"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<AuthorGetDto> createAuthor(@RequestBody @Valid AuthorCreateDto dto) {
        Author createdAuthor = authorService.createAuthor(dto);
        return ResponseEntity.ok(new AuthorGetDto(createdAuthor));
    }

    @Operation(summary = "Update an author", description = "Update an existing author by their ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Author updated"),
        @ApiResponse(responseCode = "404", description = "Author not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @Parameter(description = "ID of the author to update", name = "id")
    @PutMapping("/{id}")
    public ResponseEntity<AuthorGetDto> updateAuthor(
         @PathVariable int id,
         @RequestBody @Valid AuthorUpdateDto dto
    ) {
        Author updatedAuthor = authorService.updateAuthor(id, dto);
        return ResponseEntity.ok(new AuthorGetDto(updatedAuthor));
    }

    @Operation(summary = "Delete an author", description = "Delete an author by their ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Author deleted"),
        @ApiResponse(responseCode = "404", description = "Author not found")
    })
    @Parameter(description = "ID of the author to delete", name = "id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable int id) {
        authorService.deleteAuthor(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Search authors by name", description = "Search for authors by their name")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation"),
        @ApiResponse(responseCode = "404", description = "Authors not found")
    })
    @Parameter(description = "Name or part of the name to search for", name = "name")
    @GetMapping("/search")
    public ResponseEntity<List<AuthorGetDto>> searchAuthors(
        @RequestParam("name") String name
    ) {
        List<Author> authors = authorService.findAuthorsByName(name);
        List<AuthorGetDto> dtos = authors.stream().map(AuthorGetDto::new).toList();
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Get authors by book category",
            description = "Retrieve authors who have books in a specific category")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation"),
        @ApiResponse(responseCode = "404", description = "Authors not found")
    })
    @Parameter(description = "Category name to filter authors by", name = "category")
    @GetMapping("/by-category")
    public ResponseEntity<List<AuthorGetDto>> getAuthorsByBookCategory(
            @RequestParam("category") String category) {

        List<Author> authors = authorService.findAuthorsByBookCategory(category);
        List<AuthorGetDto> dtos = authors.stream().map(AuthorGetDto::new).toList();
        return ResponseEntity.ok(dtos);
    }
}
