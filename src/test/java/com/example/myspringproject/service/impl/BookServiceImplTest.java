
package com.example.myspringproject.service.impl;

import com.example.myspringproject.cache.BookCache;
import com.example.myspringproject.cache.CategoryCache;
import com.example.myspringproject.dto.create.BookCreateDto;
import com.example.myspringproject.dto.update.BookUpdateDto;
import com.example.myspringproject.exception.ValidationException;
import com.example.myspringproject.model.Author;
import com.example.myspringproject.model.Book;
import com.example.myspringproject.model.Category;
import com.example.myspringproject.repository.AuthorRepository;
import com.example.myspringproject.repository.BookRepository;
import com.example.myspringproject.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private AuthorRepository authorRepository;
    @Mock
    private BookCache bookCache;
    @Mock
    private CategoryCache categoryCache;

    @InjectMocks
    private BookServiceImpl bookServiceImpl;

    private Book book1;
    private Book book2;
    private Author author1;
    private Category category1;
    private Category category2;

    @BeforeEach
    void setUp() {
        author1 = new Author();
        author1.setAuthorId(1);
        author1.setAuthorName("Test Author");
        author1.setBooks(new ArrayList<>());

        category1 = new Category();
        category1.setCategoryId(1);
        category1.setCategoryName("Fiction");
        category1.setBooks(new ArrayList<>());

        category2 = new Category();
        category2.setCategoryId(2);
        category2.setCategoryName("Sci-Fi");
        category2.setBooks(new ArrayList<>());

        book1 = new Book();
        book1.setBookId(1);
        book1.setBookName("Test Book 1");
        book1.setAuthor(author1);
        book1.setCategories(new ArrayList<>(List.of(category1)));
        author1.getBooks().add(book1);
        category1.getBooks().add(book1);

        book2 = new Book();
        book2.setBookId(2);
        book2.setBookName("Test Book 2");
        book2.setAuthor(author1);
        book2.setCategories(new ArrayList<>(List.of(category1, category2)));
        author1.getBooks().add(book2);
        category1.getBooks().add(book2);
        category2.getBooks().add(book2);
    }

    @Test
    void findAllBooks_shouldReturnAllBooks() {

        when(bookRepository.findAllWithCategoriesAndAuthor()).thenReturn(List.of(book1, book2));


        List<Book> result = bookServiceImpl.findAllBooks();


        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bookRepository, times(1)).findAllWithCategoriesAndAuthor();
    }

    @Test
    void findBookById_whenCacheHit_shouldReturnBookFromCache() {

        int bookId = 1;
        String cacheKey = "book_id_" + bookId;
        when(bookCache.containsKey(cacheKey)).thenReturn(true);
        when(bookCache.get(cacheKey)).thenReturn(List.of(book1));


        Book result = bookServiceImpl.findBookById(bookId);


        assertNotNull(result);
        assertEquals(bookId, result.getBookId());
        verify(bookCache, times(1)).containsKey(cacheKey);
        verify(bookCache, times(1)).get(cacheKey);
        verify(bookRepository, never()).findById(anyInt());
    }

    @Test
    void findBookById_whenCacheMissAndFound_shouldReturnBookFromRepoAndCache() {

        int bookId = 1;
        String cacheKey = "book_id_" + bookId;
        when(bookCache.containsKey(cacheKey)).thenReturn(false);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book1));


        Book result = bookServiceImpl.findBookById(bookId);


        assertNotNull(result);
        assertEquals(bookId, result.getBookId());
        verify(bookCache, times(1)).containsKey(cacheKey);
        verify(bookCache, never()).get(anyString());
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookCache, times(1)).put(cacheKey, List.of(book1));
    }

    @Test
    void findBookById_whenCacheMissAndNotFound_shouldThrowException() {

        int bookId = 99;
        String cacheKey = "book_id_" + bookId;
        when(bookCache.containsKey(cacheKey)).thenReturn(false);
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());


        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> bookServiceImpl.findBookById(bookId)
        );
        assertEquals("Книга не найдена по id:" + bookId, exception.getMessage());
        verify(bookCache, times(1)).containsKey(cacheKey);
        verify(bookCache, never()).get(anyString());
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookCache, never()).put(anyString(), anyList());
    }

    @Test
    void searchBooks_whenCacheHit_shouldReturnBooksFromCache() {

        String author = "Test Author";
        String title = "Test Book";
        String cacheKey = "searchBooks_" + author + "_" + title;
        when(bookCache.containsKey(cacheKey)).thenReturn(true);
        when(bookCache.get(cacheKey)).thenReturn(List.of(book1, book2));


        List<Book> result = bookServiceImpl.searchBooks(author, title);


        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bookCache, times(1)).containsKey(cacheKey);
        verify(bookCache, times(1)).get(cacheKey);
        verify(bookRepository, never()).findByAuthorAuthorNameContainingIgnoreCaseOrBookNameContainingIgnoreCase(anyString(), anyString());
    }

    @Test
    void searchBooks_whenCacheMissAndFound_shouldReturnBooksFromRepoAndCache() {

        String author = "Test Author";
        String title = "Test Book";
        String cacheKey = "searchBooks_" + author + "_" + title;
        when(bookCache.containsKey(cacheKey)).thenReturn(false);
        when(bookRepository.findByAuthorAuthorNameContainingIgnoreCaseOrBookNameContainingIgnoreCase(author, title))
                .thenReturn(List.of(book1, book2));


        List<Book> result = bookServiceImpl.searchBooks(author, title);


        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bookCache, times(1)).containsKey(cacheKey);
        verify(bookCache, never()).get(anyString());
        verify(bookRepository, times(1)).findByAuthorAuthorNameContainingIgnoreCaseOrBookNameContainingIgnoreCase(author, title);
        verify(bookCache, times(1)).put(cacheKey, List.of(book1, book2));
    }

    @Test
    void searchBooks_whenCacheMissAndNotFound_shouldThrowException() {

        String author = "Unknown Author";
        String title = "Unknown Title";
        String cacheKey = "searchBooks_" + author + "_" + title;
        when(bookCache.containsKey(cacheKey)).thenReturn(false);
        when(bookRepository.findByAuthorAuthorNameContainingIgnoreCaseOrBookNameContainingIgnoreCase(author, title))
                .thenReturn(Collections.emptyList());


        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> bookServiceImpl.searchBooks(author, title)
        );
        assertTrue(exception.getMessage().contains("Книги не найдена по автору:"));
        verify(bookCache, times(1)).containsKey(cacheKey);
        verify(bookCache, never()).get(anyString());
        verify(bookRepository, times(1)).findByAuthorAuthorNameContainingIgnoreCaseOrBookNameContainingIgnoreCase(author, title);
        verify(bookCache, never()).put(anyString(), anyList());
    }

    @Test
    void findBooksByCategoryId_whenCategoryExistsAndCacheMiss_shouldReturnBooks() {
        int categoryId = 1;
        String cacheKey = "booksByCategoryId_" + categoryId;
        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        when(bookCache.containsKey(cacheKey)).thenReturn(false);
        when(bookRepository.findByCategoryId(categoryId)).thenReturn(List.of(book1, book2));

        List<Book> result = bookServiceImpl.findBooksByCategoryId(categoryId);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(categoryRepository, times(1)).existsById(categoryId);
        verify(bookCache, times(1)).containsKey(cacheKey);
        verify(bookRepository, times(1)).findByCategoryId(categoryId);
        verify(bookCache, times(1)).put(cacheKey, List.of(book1, book2));
    }

    @Test
    void findBooksByCategoryId_whenCategoryNotFound_shouldThrowException() {
        int categoryId = 99;
        when(categoryRepository.existsById(categoryId)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> bookServiceImpl.findBooksByCategoryId(categoryId)
        );

        assertEquals("Категория не найдена с id: " + categoryId, exception.getMessage());
        verify(categoryRepository, times(1)).existsById(categoryId);
        verify(bookCache, never()).containsKey(anyString());
        verify(bookRepository, never()).findByCategoryId(anyInt());
    }

    @Test
    void findBooksByAuthorId_whenAuthorExistsAndCacheMiss_shouldReturnBooks() {
        int authorId = 1;
        String cacheKey = "booksByAuthorId_" + authorId;
        when(authorRepository.existsById(authorId)).thenReturn(true);
        when(bookCache.containsKey(cacheKey)).thenReturn(false);
        when(bookRepository.findByAuthorId(authorId)).thenReturn(List.of(book1, book2));

        List<Book> result = bookServiceImpl.findBooksByAuthorId(authorId);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(authorRepository, times(1)).existsById(authorId);
        verify(bookCache, times(1)).containsKey(cacheKey);
        verify(bookRepository, times(1)).findByAuthorId(authorId);
        verify(bookCache, times(1)).put(cacheKey, List.of(book1, book2));
    }

    @Test
    void findBooksByAuthorId_whenAuthorNotFound_shouldThrowException() {
        int authorId = 99;
        when(authorRepository.existsById(authorId)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> bookServiceImpl.findBooksByAuthorId(authorId)
        );

        assertEquals("Автор не найден с id: " + authorId, exception.getMessage());
        verify(authorRepository, times(1)).existsById(authorId);
        verify(bookCache, never()).containsKey(anyString());
        verify(bookRepository, never()).findByAuthorId(anyInt());
    }


    @Test
    void createBook_whenValidDto_shouldCreateAndReturnBook() {

        BookCreateDto dto = new BookCreateDto();
        dto.setName("New Book");
        dto.setAuthorId(1);
        dto.setCategoryIds(List.of(1, 2));

        when(bookRepository.findByAuthorId(dto.getAuthorId())).thenReturn(Collections.emptyList());
        when(authorRepository.findById(dto.getAuthorId())).thenReturn(Optional.of(author1));
        when(categoryRepository.findAllById(dto.getCategoryIds())).thenReturn(List.of(category1, category2));

        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book bookToSave = invocation.getArgument(0);
            bookToSave.setBookId(3);
            return bookToSave;
        });


        Book result = bookServiceImpl.createBook(dto);


        assertNotNull(result);
        assertEquals("New Book", result.getBookName());
        assertEquals(author1, result.getAuthor());
        assertEquals(2, result.getCategories().size());
        assertTrue(result.getCategories().contains(category1));
        assertTrue(result.getCategories().contains(category2));
        assertTrue(author1.getBooks().contains(result));
        assertTrue(category1.getBooks().contains(result));
        assertTrue(category2.getBooks().contains(result));

        verify(bookRepository, times(1)).findByAuthorId(dto.getAuthorId());
        verify(authorRepository, times(1)).findById(dto.getAuthorId());
        verify(categoryRepository, times(1)).findAllById(dto.getCategoryIds());
        verify(bookRepository, times(1)).save(any(Book.class));
        verify(bookCache, times(1)).clear();
        verify(categoryCache, times(1)).clear();
    }

    @Test
    void createBook_whenAuthorNotFound_shouldThrowException() {

        BookCreateDto dto = new BookCreateDto();
        dto.setName("New Book");
        dto.setAuthorId(99);
        dto.setCategoryIds(List.of(1));

        when(bookRepository.findByAuthorId(dto.getAuthorId())).thenReturn(Collections.emptyList());
        when(authorRepository.findById(dto.getAuthorId())).thenReturn(Optional.empty());


        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> bookServiceImpl.createBook(dto)
        );
        assertEquals("Author not found with ID: " + dto.getAuthorId(), exception.getMessage());

        verify(bookRepository, times(1)).findByAuthorId(dto.getAuthorId());
        verify(authorRepository, times(1)).findById(dto.getAuthorId());
        verify(categoryRepository, never()).findAllById(anyList());
        verify(bookRepository, never()).save(any(Book.class));
        verify(bookCache, never()).clear();
        verify(categoryCache, never()).clear();
    }

    @Test
    void createBook_whenCategoryNotFound_shouldThrowException() {

        BookCreateDto dto = new BookCreateDto();
        dto.setName("New Book");
        dto.setAuthorId(1);
        dto.setCategoryIds(List.of(1, 99));

        when(bookRepository.findByAuthorId(dto.getAuthorId())).thenReturn(Collections.emptyList());
        when(authorRepository.findById(dto.getAuthorId())).thenReturn(Optional.of(author1));
        when(categoryRepository.findAllById(dto.getCategoryIds())).thenReturn(List.of(category1));


        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bookServiceImpl.createBook(dto)
        );
        assertEquals("Some categories not found", exception.getMessage());

        verify(bookRepository, times(1)).findByAuthorId(dto.getAuthorId());
        verify(authorRepository, times(1)).findById(dto.getAuthorId());
        verify(categoryRepository, times(1)).findAllById(dto.getCategoryIds());
        verify(bookRepository, never()).save(any(Book.class));
        verify(bookCache, never()).clear();
        verify(categoryCache, never()).clear();
    }

    @Test
    void createBook_whenDuplicateBookForAuthor_shouldThrowValidationException() {

        BookCreateDto dto = new BookCreateDto();
        dto.setName("Test Book 1");
        dto.setAuthorId(1);


        when(bookRepository.findByAuthorId(dto.getAuthorId())).thenReturn(List.of(book1));


        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookServiceImpl.createBook(dto)
        );
        assertEquals(1, exception.getErrors().size());
        assertEquals("У автора уже есть книга с таким названием", exception.getErrors().get(0));

        verify(bookRepository, times(1)).findByAuthorId(dto.getAuthorId());
        verify(authorRepository, never()).findById(anyInt());
        verify(categoryRepository, never()).findAllById(anyList());
        verify(bookRepository, never()).save(any(Book.class));
        verify(bookCache, never()).clear();
        verify(categoryCache, never()).clear();
    }


    @Test
    void updateBook_whenValidDto_shouldUpdateAndReturnBook() {

        int bookId = 1;
        BookUpdateDto dto = new BookUpdateDto();
        dto.setBookName("Updated Book Name");
        dto.setAuthorId(1);
        dto.setCategoriesIds(List.of(2));

        Author existingAuthor = new Author();
        existingAuthor.setAuthorId(1);
        existingAuthor.setAuthorName("Test Author");

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book1));
        when(authorRepository.findById(dto.getAuthorId())).thenReturn(Optional.of(existingAuthor));
        when(categoryRepository.findAllById(dto.getCategoriesIds())).thenReturn(List.of(category2));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));


        Book result = bookServiceImpl.updateBook(bookId, dto);


        assertNotNull(result);
        assertEquals(bookId, result.getBookId());
        assertEquals("Updated Book Name", result.getBookName());
        assertEquals(existingAuthor, result.getAuthor());
        assertEquals(1, result.getCategories().size());
        assertTrue(result.getCategories().contains(category2));

        verify(bookRepository, times(1)).findById(bookId);
        verify(authorRepository, times(1)).findById(dto.getAuthorId());
        verify(categoryRepository, times(1)).findAllById(dto.getCategoriesIds());
        verify(bookRepository, times(1)).save(book1);
        verify(bookCache, times(1)).clear();
        verify(categoryCache, times(1)).clear();
    }

    @Test
    void updateBook_whenBookNotFound_shouldThrowException() {

        int bookId = 99;
        BookUpdateDto dto = new BookUpdateDto();
        dto.setBookName("Update Attempt");

        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());


        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> bookServiceImpl.updateBook(bookId, dto)
        );
        assertEquals("Book not found", exception.getMessage());

        verify(bookRepository, times(1)).findById(bookId);
        verify(authorRepository, never()).findById(anyInt());
        verify(categoryRepository, never()).findAllById(anyList());
        verify(bookRepository, never()).save(any(Book.class));
        verify(bookCache, never()).clear();
        verify(categoryCache, never()).clear();
    }

    @Test
    void updateBook_whenAuthorNotFound_shouldThrowException() {

        int bookId = 1;
        BookUpdateDto dto = new BookUpdateDto();
        dto.setBookName("Update Attempt");
        dto.setAuthorId(99);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book1));
        when(authorRepository.findById(dto.getAuthorId())).thenReturn(Optional.empty());


        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> bookServiceImpl.updateBook(bookId, dto)
        );
        assertEquals("Author not found", exception.getMessage());

        verify(bookRepository, times(1)).findById(bookId);
        verify(authorRepository, times(1)).findById(dto.getAuthorId());
        verify(categoryRepository, never()).findAllById(anyList());
        verify(bookRepository, never()).save(any(Book.class));
        verify(bookCache, never()).clear();
        verify(categoryCache, never()).clear();
    }


    @Test
    void deleteBookById_whenBookExists_shouldDeleteBook() {

        int bookId = 1;

        Author authorWithBook = new Author();
        authorWithBook.setAuthorId(1);
        authorWithBook.setAuthorName("Test Author");
        List<Book> authorBooks = new ArrayList<>();
        authorBooks.add(book1);
        authorWithBook.setBooks(authorBooks);
        book1.setAuthor(authorWithBook);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book1));
        doNothing().when(bookRepository).deleteById(bookId);


        bookServiceImpl.deleteBookById(bookId);


        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, times(1)).deleteById(bookId);
        verify(bookCache, times(1)).clear();
        verify(categoryCache, times(1)).clear();

        assertFalse(authorWithBook.getBooks().contains(book1));
    }

    @Test
    void deleteBookById_whenBookNotFound_shouldThrowException() {

        int bookId = 99;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());


        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> bookServiceImpl.deleteBookById(bookId)
        );
        assertEquals("Book not found", exception.getMessage());

        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, never()).deleteById(anyInt());
        verify(bookCache, never()).clear();
        verify(categoryCache, never()).clear();
    }



    @Test
    void createBooks_whenValidDtosWithIds_shouldCreateBooks() {

        BookCreateDto dto1 = new BookCreateDto();
        dto1.setName("Bulk Book 1");
        dto1.setAuthorId(1);
        dto1.setCategoryIds(List.of(1));

        BookCreateDto dto2 = new BookCreateDto();
        dto2.setName("Bulk Book 2");
        dto2.setAuthorId(1);
        dto2.setCategoryIds(List.of(2));

        List<BookCreateDto> dtos = List.of(dto1, dto2);


        when(authorRepository.findAllById(Set.of(1))).thenReturn(List.of(author1));
        when(categoryRepository.findAllById(Set.of(1, 2))).thenReturn(List.of(category1, category2));
        when(bookRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        when(authorRepository.findByAuthorNameIn(Collections.emptySet())).thenReturn(Collections.emptyList());
        when(categoryRepository.findByCategoryNameIn(Collections.emptySet())).thenReturn(Collections.emptyList());



        List<Book> result = bookServiceImpl.createBooks(dtos);


        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Bulk Book 1", result.get(0).getBookName());
        assertEquals("Bulk Book 2", result.get(1).getBookName());
        assertEquals(author1, result.get(0).getAuthor());
        assertEquals(author1, result.get(1).getAuthor());
        assertTrue(result.get(0).getCategories().contains(category1));
        assertTrue(result.get(1).getCategories().contains(category2));

        assertTrue(author1.getBooks().stream().anyMatch(b -> b.getBookName().equals("Bulk Book 1")));
        assertTrue(author1.getBooks().stream().anyMatch(b -> b.getBookName().equals("Bulk Book 2")));
        assertTrue(category1.getBooks().stream().anyMatch(b -> b.getBookName().equals("Bulk Book 1")));
        assertTrue(category2.getBooks().stream().anyMatch(b -> b.getBookName().equals("Bulk Book 2")));


        verify(authorRepository, times(1)).findAllById(Set.of(1));
        verify(categoryRepository, times(1)).findAllById(Set.of(1, 2));

        verify(authorRepository, times(1)).findByAuthorNameIn(Collections.emptySet());
        verify(categoryRepository, times(1)).findByCategoryNameIn(Collections.emptySet());
        verify(authorRepository, never()).saveAll(anyList());
        verify(categoryRepository, never()).saveAll(anyList());
        verify(bookRepository, times(1)).saveAll(anyList());
        verify(bookCache, times(1)).clear();
        verify(categoryCache, times(1)).clear();
    }

    @Test
    void createBooks_whenValidDtosWithNames_shouldCreateBooksAndNewEntities() {

        BookCreateDto dto1 = new BookCreateDto();
        dto1.setName("Bulk Book Name 1");
        dto1.setAuthorName("New Author");
        dto1.setCategoryNames(List.of("New Category"));

        List<BookCreateDto> dtos = List.of(dto1);

        Author newAuthor = new Author();
        newAuthor.setAuthorId(10);
        newAuthor.setAuthorName("New Author");
        newAuthor.setBooks(new ArrayList<>());

        Category newCategory = new Category();
        newCategory.setCategoryId(10);
        newCategory.setCategoryName("New Category");
        newCategory.setBooks(new ArrayList<>());


        when(authorRepository.findByAuthorNameIn(Set.of("New Author"))).thenReturn(Collections.emptyList());
        when(categoryRepository.findByCategoryNameIn(Set.of("New Category"))).thenReturn(Collections.emptyList());
        when(authorRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<Author> authorsToSave = invocation.getArgument(0);
            authorsToSave.get(0).setAuthorId(10);
            return authorsToSave;
        });
        when(categoryRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<Category> catsToSave = invocation.getArgument(0);
            catsToSave.get(0).setCategoryId(10);
            return catsToSave;
        });
        when(bookRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        when(authorRepository.findAllById(Collections.emptySet())).thenReturn(Collections.emptyList());
        when(categoryRepository.findAllById(Collections.emptySet())).thenReturn(Collections.emptyList());



        List<Book> result = bookServiceImpl.createBooks(dtos);


        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Bulk Book Name 1", result.get(0).getBookName());
        assertEquals("New Author", result.get(0).getAuthor().getAuthorName());
        assertEquals(10, result.get(0).getAuthor().getAuthorId());
        assertEquals("New Category", result.get(0).getCategories().get(0).getCategoryName());
        assertEquals(10, result.get(0).getCategories().get(0).getCategoryId());

        verify(authorRepository, times(1)).findByAuthorNameIn(Set.of("New Author"));
        verify(categoryRepository, times(1)).findByCategoryNameIn(Set.of("New Category"));
        verify(authorRepository, times(1)).saveAll(anyList());
        verify(categoryRepository, times(1)).saveAll(anyList());
        verify(bookRepository, times(1)).saveAll(anyList());
        verify(bookCache, times(1)).clear();
        verify(categoryCache, times(1)).clear();
    }

    @Test
    void createBooks_whenDtoHasBothAuthorIdAndName_shouldThrowValidationException() {

        BookCreateDto dto1 = new BookCreateDto();
        dto1.setName("Invalid Book");
        dto1.setAuthorId(1);
        dto1.setAuthorName("Invalid Author Name");

        List<BookCreateDto> dtos = List.of(dto1);


        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookServiceImpl.createBooks(dtos)
        );
        assertTrue(exception.getMessage().contains("Validation failed"));
        assertEquals(1, exception.getErrors().size());
        assertTrue(exception.getErrors().get(0).contains("указан либо authorId, либо authorName"));

        verify(bookRepository, never()).saveAll(anyList());
        verify(bookCache, never()).clear();
        verify(categoryCache, never()).clear();
    }

    @Test
    void createBooks_whenAuthorIdNotFound_shouldThrowEntityNotFoundException() {

        BookCreateDto dto1 = new BookCreateDto();
        dto1.setName("Book With Missing Author");
        dto1.setAuthorId(99);
        dto1.setCategoryIds(List.of(1));

        List<BookCreateDto> dtos = List.of(dto1);

        when(authorRepository.findAllById(Set.of(99))).thenReturn(Collections.emptyList());
        when(categoryRepository.findAllById(Set.of(1))).thenReturn(List.of(category1));

        when(authorRepository.findByAuthorNameIn(Collections.emptySet())).thenReturn(Collections.emptyList());
        when(categoryRepository.findByCategoryNameIn(Collections.emptySet())).thenReturn(Collections.emptyList());



        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> bookServiceImpl.createBooks(dtos)
        );
        assertEquals("Автор не найден с ID: 99", exception.getMessage());

        verify(authorRepository, times(1)).findAllById(Set.of(99));
        verify(categoryRepository, times(1)).findAllById(Set.of(1));
        verify(bookRepository, never()).saveAll(anyList());
        verify(bookCache, never()).clear();
        verify(categoryCache, never()).clear();
    }

    @Test
    void createBooks_whenCategoryIdNotFound_shouldThrowEntityNotFoundException() {

        BookCreateDto dto1 = new BookCreateDto();
        dto1.setName("Book With Missing Category");
        dto1.setAuthorId(1);
        dto1.setCategoryIds(List.of(99));

        List<BookCreateDto> dtos = List.of(dto1);

        when(authorRepository.findAllById(Set.of(1))).thenReturn(List.of(author1));
        when(categoryRepository.findAllById(Set.of(99))).thenReturn(Collections.emptyList());

        when(authorRepository.findByAuthorNameIn(Collections.emptySet())).thenReturn(Collections.emptyList());
        when(categoryRepository.findByCategoryNameIn(Collections.emptySet())).thenReturn(Collections.emptyList());



        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> bookServiceImpl.createBooks(dtos)
        );
        assertEquals("Категория не найдена с ID: 99", exception.getMessage());

        verify(authorRepository, times(1)).findAllById(Set.of(1));
        verify(categoryRepository, times(1)).findAllById(Set.of(99));
        verify(bookRepository, never()).saveAll(anyList());
        verify(bookCache, never()).clear();
        verify(categoryCache, never()).clear();
    }



    @Test
    void findBooksByCategory_whenCacheHit_shouldReturnBooksFromCache() {

        String categoryName = "Fiction";
        String cacheKey = "booksByCategory_" + categoryName;
        List<Book> expectedBooks = List.of(book1, book2);
        when(bookCache.containsKey(cacheKey)).thenReturn(true);
        when(bookCache.get(cacheKey)).thenReturn(expectedBooks);


        List<Book> result = bookServiceImpl.findBooksByCategory(categoryName);


        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedBooks, result);
        verify(bookCache, times(1)).containsKey(cacheKey);
        verify(bookCache, times(1)).get(cacheKey);
        verify(bookRepository, never()).findByCategoryName(anyString());
    }

    @Test
    void findBooksByCategory_whenCacheMissAndFound_shouldReturnBooksFromRepoAndCache() {

        String categoryName = "Fiction";
        String cacheKey = "booksByCategory_" + categoryName;
        List<Book> expectedBooks = List.of(book1, book2);
        when(bookCache.containsKey(cacheKey)).thenReturn(false);
        when(bookRepository.findByCategoryName(categoryName)).thenReturn(expectedBooks);


        List<Book> result = bookServiceImpl.findBooksByCategory(categoryName);


        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedBooks, result);
        verify(bookCache, times(1)).containsKey(cacheKey);
        verify(bookCache, never()).get(anyString());
        verify(bookRepository, times(1)).findByCategoryName(categoryName);
        verify(bookCache, times(1)).put(cacheKey, expectedBooks);
    }

    @Test
    void findBooksByCategory_whenCacheMissAndNotFound_shouldThrowException() {

        String categoryName = "NonExistent";
        String cacheKey = "booksByCategory_" + categoryName;
        when(bookCache.containsKey(cacheKey)).thenReturn(false);
        when(bookRepository.findByCategoryName(categoryName)).thenReturn(Collections.emptyList());


        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> bookServiceImpl.findBooksByCategory(categoryName)
        );
        assertEquals("Книги не найдены по категории: " + categoryName, exception.getMessage());
        verify(bookCache, times(1)).containsKey(cacheKey);
        verify(bookCache, never()).get(anyString());
        verify(bookRepository, times(1)).findByCategoryName(categoryName);
        verify(bookCache, never()).put(anyString(), anyList());
    }

    @Test
    void findBooksByCategory_whenCacheHitButEmpty_shouldThrowException() {

        String categoryName = "EmptyCategory";
        String cacheKey = "booksByCategory_" + categoryName;
        when(bookCache.containsKey(cacheKey)).thenReturn(true);
        when(bookCache.get(cacheKey)).thenReturn(Collections.emptyList());


        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> bookServiceImpl.findBooksByCategory(categoryName)
        );
        assertEquals("Книги не найдены по категории: " + categoryName, exception.getMessage());
        verify(bookCache, times(1)).containsKey(cacheKey);
        verify(bookCache, times(1)).get(cacheKey);
        verify(bookRepository, never()).findByCategoryName(anyString());
    }




    @Test
    void findBooksByAuthor_whenCacheHit_shouldReturnBooksFromCache() {

        String authorName = "Test Author";
        String cacheKey = "booksByAuthor_" + authorName;
        List<Book> expectedBooks = List.of(book1, book2);
        when(bookCache.containsKey(cacheKey)).thenReturn(true);
        when(bookCache.get(cacheKey)).thenReturn(expectedBooks);


        List<Book> result = bookServiceImpl.findBooksByAuthor(authorName);


        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedBooks, result);
        verify(bookCache, times(1)).containsKey(cacheKey);
        verify(bookCache, times(1)).get(cacheKey);
        verify(bookRepository, never()).findByAuthorName(anyString());
    }

    @Test
    void findBooksByAuthor_whenCacheMissAndFound_shouldReturnBooksFromRepoAndCache() {

        String authorName = "Test Author";
        String cacheKey = "booksByAuthor_" + authorName;
        List<Book> expectedBooks = List.of(book1, book2);
        when(bookCache.containsKey(cacheKey)).thenReturn(false);
        when(bookRepository.findByAuthorName(authorName)).thenReturn(expectedBooks);


        List<Book> result = bookServiceImpl.findBooksByAuthor(authorName);


        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedBooks, result);
        verify(bookCache, times(1)).containsKey(cacheKey);
        verify(bookCache, never()).get(anyString());
        verify(bookRepository, times(1)).findByAuthorName(authorName);
        verify(bookCache, times(1)).put(cacheKey, expectedBooks);
    }

    @Test
    void findBooksByAuthor_whenCacheMissAndNotFound_shouldThrowException() {

        String authorName = "NonExistent Author";
        String cacheKey = "booksByAuthor_" + authorName;
        when(bookCache.containsKey(cacheKey)).thenReturn(false);
        when(bookRepository.findByAuthorName(authorName)).thenReturn(Collections.emptyList());


        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> bookServiceImpl.findBooksByAuthor(authorName)
        );
        assertEquals("Книги не найдены по автору: " + authorName, exception.getMessage());
        verify(bookCache, times(1)).containsKey(cacheKey);
        verify(bookCache, never()).get(anyString());
        verify(bookRepository, times(1)).findByAuthorName(authorName);
        verify(bookCache, never()).put(anyString(), anyList());
    }

    @Test
    void findBooksByAuthor_whenCacheHitButEmpty_shouldThrowException() {

        String authorName = "AuthorWithNoBooks";
        String cacheKey = "booksByAuthor_" + authorName;
        when(bookCache.containsKey(cacheKey)).thenReturn(true);
        when(bookCache.get(cacheKey)).thenReturn(Collections.emptyList());


        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> bookServiceImpl.findBooksByAuthor(authorName)
        );
        assertEquals("Книги не найдены по автору: " + authorName, exception.getMessage());
        verify(bookCache, times(1)).containsKey(cacheKey);
        verify(bookCache, times(1)).get(cacheKey);
        verify(bookRepository, never()).findByAuthorName(anyString());
    }
}