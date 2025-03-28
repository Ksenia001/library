package com.example.myspringproject.controller;

import com.example.myspringproject.dto.create.BookCreateDto;
import com.example.myspringproject.dto.get.BookGetDto;
import com.example.myspringproject.dto.update.BookUpdateDto;
import com.example.myspringproject.model.Book;
import com.example.myspringproject.service.BookService;
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
@RequestMapping("/api/v2/books")
@AllArgsConstructor
public class ControllerBook {

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<List<BookGetDto>> findAllBooks() {
        List<Book> books = bookService.findAllBooks();
        List<BookGetDto> dtos = books.stream()
                .map(BookGetDto::new)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<BookGetDto> saveBook(@RequestBody @Valid BookCreateDto dto) {
        Book book = bookService.createBook(dto);
        return ResponseEntity.ok(new BookGetDto(book));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookGetDto> findBookById(@PathVariable int id) {
        Book book = bookService.findBookById(id);
        return ResponseEntity.ok(new BookGetDto(book));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookGetDto> updateBook(
            @PathVariable int id, @RequestBody @Valid BookUpdateDto dto
    ) {
        Book updatedBook = bookService.updateBook(id, dto);
        return ResponseEntity.ok(new BookGetDto(updatedBook));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteBookById(@PathVariable int id) {
        bookService.deleteBookById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<BookGetDto>> searchBooks(
            @RequestParam(value = "authorName", required = false) String authorName,
            @RequestParam(value = "title", required = false) String title
    ) {
        List<Book> result = bookService.searchBooks(authorName, title);
        List<BookGetDto> dtos = result.stream().map(BookGetDto::new).toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/by-category")
    public ResponseEntity<List<BookGetDto>> getBooksByCategory(
            @RequestParam("category") String categoryName
    ) {
        List<Book> books = bookService.findBooksByCategory(categoryName);
        List<BookGetDto> dtos = books.stream()
                .map(BookGetDto::new)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<List<BookGetDto>> getBooksByCategoryId(
            @PathVariable int categoryId
    ) {
        List<Book> books = bookService.findBooksByCategoryId(categoryId);
        List<BookGetDto> dtos = books.stream()
                .map(BookGetDto::new)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/by-author")
    public ResponseEntity<List<BookGetDto>> getBooksByAuthor(
            @RequestParam("author") String authorName
    ) {
        List<Book> books = bookService.findBooksByAuthor(authorName);
        List<BookGetDto> dtos = books.stream()
                .map(BookGetDto::new)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/by-author/{authorId}")
    public ResponseEntity<List<BookGetDto>> getBooksByAuthorId(
            @PathVariable int authorId
    ) {
        List<Book> books = bookService.findBooksByAuthorId(authorId);
        List<BookGetDto> dtos = books.stream()
                .map(BookGetDto::new)
                .toList();
        return ResponseEntity.ok(dtos);
    }
}
