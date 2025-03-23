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

    @PutMapping
    public ResponseEntity<BookGetDto> updateBook(@RequestBody @Valid BookUpdateDto dto) {
        Book updatedBook = bookService.updateBook(dto);
        return ResponseEntity.ok(new BookGetDto(updatedBook));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteBookById(@PathVariable int id) {
        bookService.deleteBookById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<BookGetDto>> searchBooks(
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "title", required = false) String title
    ) {
        List<Book> result = bookService.searchBooks(author, title);
        List<BookGetDto> dtos = result.stream()
                .map(BookGetDto::new)
                .toList();
        return ResponseEntity.ok(dtos);
    }
}
