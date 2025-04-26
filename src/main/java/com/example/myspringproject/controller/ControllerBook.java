package com.example.myspringproject.controller;

import com.example.myspringproject.dto.create.BookCreateDto;
import com.example.myspringproject.dto.create.BulkCreateDto;
import com.example.myspringproject.dto.get.BookGetDto;
import com.example.myspringproject.dto.update.BookUpdateDto;
import com.example.myspringproject.model.Book;
import com.example.myspringproject.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
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
@Tag(name = "Books", description = "API for managing books")
public class ControllerBook {

    private final BookService bookService;

    @Operation(summary = "Create many books", description = "Creates many books at once")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Books created successfully"),
        @ApiResponse(responseCode = "400", description = "Incorrect entered data")
    })
    @PostMapping("/bulk")
    public ResponseEntity<List<BookGetDto>> createBooks(
            @Parameter(description = "Data to create books")
            @Valid @RequestBody
            BulkCreateDto<BookCreateDto> books) {
        List<Book> createdBooks = bookService.createBooks(books.getDtos());
        List<BookGetDto> dtos = createdBooks.stream()
                .map(BookGetDto::new)
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(dtos);
    }


    @GetMapping
    @Operation(summary = "Get all books", description = "Retrieve a list of all books")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<BookGetDto>> findAllBooks() {
        List<Book> books = bookService.findAllBooks();
        List<BookGetDto> dtos = books.stream()
                .map(BookGetDto::new)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Create a new book",
            description = "Create a new book with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Book created"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<BookGetDto> saveBook(@RequestBody @Valid BookCreateDto dto) {
        Book book = bookService.createBook(dto);
        return ResponseEntity.ok(new BookGetDto(book));
    }

    @Operation(summary = "Get book by ID", description = "Retrieve a book by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Book found"),
        @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @Parameter(description = "ID of the book to retrieve", name = "id")
    @GetMapping("/{id}")
    public ResponseEntity<BookGetDto> findBookById(@PathVariable int id) {
        Book book = bookService.findBookById(id);
        return ResponseEntity.ok(new BookGetDto(book));
    }

    @Operation(summary = "Update a book", description = "Update an existing book by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Book updated"),
        @ApiResponse(responseCode = "404", description = "Book not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @Parameter(description = "ID of the book to update", name = "id")
    @PutMapping("/{id}")
    public ResponseEntity<BookGetDto> updateBook(
            @PathVariable int id, @RequestBody @Valid BookUpdateDto dto
    ) {
        Book updatedBook = bookService.updateBook(id, dto);
        return ResponseEntity.ok(new BookGetDto(updatedBook));
    }

    @Operation(summary = "Delete a book", description = "Delete a book by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Book deleted"),
        @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @Parameter(description = "ID of the book to delete", name = "id")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteBookById(@PathVariable int id) {
        bookService.deleteBookById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Search books", description = "Search for books by author name or title")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation"),
        @ApiResponse(responseCode = "404", description = "Books not found")
    })
    @Parameter(description = "Author name to search for (optional)",
            name = "authorName", required = false)
    @Parameter(description = "Book title to search for (optional)",
            name = "title", required = false)
    @GetMapping("/search")
    public ResponseEntity<List<BookGetDto>> searchBooks(
            @RequestParam(value = "authorName", required = false) String authorName,
            @RequestParam(value = "title", required = false) String title
    ) {
        List<Book> result = bookService.searchBooks(authorName, title);
        List<BookGetDto> dtos = result.stream().map(BookGetDto::new).toList();
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Get books by category name",
            description = "Retrieve books belonging to a specific category by name")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation"),
        @ApiResponse(responseCode = "404", description = "Books not found")
    })
    @Parameter(description = "Category name to filter books by", name = "category")
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

    @Operation(summary = "Get books by category ID",
            description = "Retrieve books belonging to a specific category by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation")
    })
    @Parameter(description = "ID of the category to filter books by", name = "categoryId")
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

    @Operation(summary = "Get books by author name",
            description = "Retrieve books written by a specific author by name")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation")
    })
    @Parameter(description = "Author name to filter books by", name = "author")
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

    @Operation(summary = "Get books by author ID",
            description = "Retrieve books written by a specific author by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation")
    })
    @Parameter(description = "ID of the author to filter books by", name = "authorId")
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
