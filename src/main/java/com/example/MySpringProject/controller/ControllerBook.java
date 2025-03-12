package com.example.MySpringProject.controller;

import com.example.MySpringProject.service.BookService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.example.MySpringProject.model.Book;

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
    public Book findBookById(@PathVariable("id") int id) {
        return service.findBookById(id);
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
    public List<Book> searchBooks(
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "title", required = false) String title
    ) {

        if (author != null && !author.isBlank()) {
            return service.findBooksByAuthor(author);
        }

        else if (title != null && !title.isBlank()) {
            return service.findBooksByName(title);
        }

        return List.of();
    }


}
