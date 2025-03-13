package com.example.myspringproject.controller;

import com.example.myspringproject.model.Book;
import com.example.myspringproject.service.BookService;
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
@RequestMapping("/api/v1/books")
@AllArgsConstructor
public class ControllerBook {

    private final BookService service;

    @GetMapping
    public List<Book> findAllBooks() {
        return service.findAllBooks();
    }

    @PostMapping("/save_book")
    public String saveBook(@RequestBody Book book) {
        service.saveBook(book);
        return "Книга успешно добавлена";
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> findBookById(@PathVariable("id") int id) {
        Book foundBook = service.findBookById(id);
        if (foundBook == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(foundBook);
    }

    @PutMapping("/update_book")
    public Book updateBook(@RequestBody Book book) {
        return service.updateBook(book);
    }

    @DeleteMapping("/delete_book/{id}")
    public void deleteBookById(@PathVariable int id) {
        service.deleteBookById(id);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Book>> searchBooks(
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "title", required = false) String title
    ) {
        List<Book> result;

        if (author != null && !author.isBlank()) {
            result = service.findBooksByAuthor(author);
        } else if (title != null && !title.isBlank()) {
            result = service.findBooksByName(title);
        } else {
            result = List.of();
        }

        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(result);
        }
    }

}
