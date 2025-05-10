// file: src/test/java/com/example/myspringproject/service/impl/AuthorServiceImplTest.java
package com.example.myspringproject.service.impl;

import com.example.myspringproject.cache.AuthorCache;
import com.example.myspringproject.dto.create.AuthorCreateDto;
import com.example.myspringproject.dto.update.AuthorUpdateDto;
import com.example.myspringproject.exception.UniqueConstraintViolationException;
import com.example.myspringproject.model.Author;
import com.example.myspringproject.model.Book;
import com.example.myspringproject.repository.AuthorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest; // Added import
import org.junit.jupiter.params.provider.Arguments; // Added import
import org.junit.jupiter.params.provider.MethodSource; // Added import
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream; // Added import

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorServiceImplTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private AuthorCache authorCache;

    @InjectMocks
    private AuthorServiceImpl authorServiceImpl;

    // Instance variables for general use in non-parameterized tests
    private Author author1;
    private Author author2;

    @BeforeEach
    void setUp() {
        // Setup for non-parameterized tests
        author1 = new Author();
        author1.setAuthorId(1);
        author1.setAuthorName("Author One");
        author1.setBooks(new ArrayList<>()); // Initialize list

        author2 = new Author();
        author2.setAuthorId(2);
        author2.setAuthorName("Author Two");
        author2.setBooks(new ArrayList<>()); // Initialize list

        // Renamed for clarity
        Book bookForAuthor1 = new Book();
        bookForAuthor1.setBookId(1);
        bookForAuthor1.setBookName("Book by Author One");
        bookForAuthor1.setAuthor(author1);
        author1.getBooks().add(bookForAuthor1); // Add book to author1's list
    }

    // --- findAllAuthors ---
    @Test
    void findAllAuthors_shouldReturnAllAuthors() {
        // Arrange
        when(authorRepository.findAllWithBooks()).thenReturn(List.of(author1, author2));

        // Act
        List<Author> result = authorServiceImpl.findAllAuthors();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(authorRepository, times(1)).findAllWithBooks();
        verifyNoInteractions(authorCache); // This method doesn't use cache
    }

    // --- findAuthorById ---
    @Test
    void findAuthorById_whenCacheHit_shouldReturnAuthorFromCache() {
        // Arrange
        int authorId = 1;
        String cacheKey = "author_id_" + authorId;
        when(authorCache.containsKey(cacheKey)).thenReturn(true);
        when(authorCache.get(cacheKey)).thenReturn(List.of(author1));

        // Act
        Author result = authorServiceImpl.findAuthorById(authorId);

        // Assert
        assertNotNull(result);
        assertEquals(authorId, result.getAuthorId());
        verify(authorCache, times(1)).containsKey(cacheKey);
        verify(authorCache, times(1)).get(cacheKey);
        verify(authorRepository, never()).findById(anyInt());
    }

    @Test
    void findAuthorById_whenCacheMissAndFound_shouldReturnAuthorFromRepoAndCache() {
        // Arrange
        int authorId = 1;
        String cacheKey = "author_id_" + authorId;
        when(authorCache.containsKey(cacheKey)).thenReturn(false);
        when(authorRepository.findById(authorId)).thenReturn(Optional.of(author1));

        // Act
        Author result = authorServiceImpl.findAuthorById(authorId);

        // Assert
        assertNotNull(result);
        assertEquals(authorId, result.getAuthorId());
        verify(authorCache, times(1)).containsKey(cacheKey);
        verify(authorCache, never()).get(anyString());
        verify(authorRepository, times(1)).findById(authorId);
        verify(authorCache, times(1)).put(cacheKey, List.of(author1));
    }

    @Test
    void findAuthorById_whenCacheMissAndNotFound_shouldThrowException() {
        // Arrange
        int authorId = 99;
        String cacheKey = "author_id_" + authorId;
        when(authorCache.containsKey(cacheKey)).thenReturn(false);
        when(authorRepository.findById(authorId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> authorServiceImpl.findAuthorById(authorId)
        );
        assertEquals("Автор не найден с id: " + authorId, exception.getMessage());
        verify(authorCache, times(1)).containsKey(cacheKey);
        verify(authorCache, never()).get(anyString());
        verify(authorRepository, times(1)).findById(authorId);
        verify(authorCache, never()).put(anyString(), anyList());
    }

    // --- findAuthorsByBookCategory ---
    @Test
    void findAuthorsByBookCategory_whenCacheHitAndNotEmpty_shouldReturnAuthorsFromCache() {
        // Arrange
        String category = "Fiction";
        String cacheKey = "authorsByCategory_" + category;
        List<Author> expectedAuthors = List.of(author1);
        when(authorCache.containsKey(cacheKey)).thenReturn(true);
        when(authorCache.get(cacheKey)).thenReturn(expectedAuthors);

        // Act
        List<Author> result = authorServiceImpl.findAuthorsByBookCategory(category);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedAuthors, result);
        verify(authorCache, times(1)).containsKey(cacheKey);
        verify(authorCache, times(1)).get(cacheKey);
        verify(authorRepository, never()).findAuthorsByBookCategory(anyString());
    }

    @Test
    void findAuthorsByBookCategory_whenCacheHitButEmpty_shouldThrowException() {
        // Arrange
        String category = "EmptyCategory";
        String cacheKey = "authorsByCategory_" + category;
        when(authorCache.containsKey(cacheKey)).thenReturn(true);
        when(authorCache.get(cacheKey)).thenReturn(Collections.emptyList()); // Cache has empty list

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> authorServiceImpl.findAuthorsByBookCategory(category)
        );
        assertEquals("Авторы не найдены по категории книги: " + category, exception.getMessage());
        verify(authorCache, times(1)).containsKey(cacheKey);
        verify(authorCache, times(1)).get(cacheKey);
        verify(authorRepository, never()).findAuthorsByBookCategory(anyString());
    }

    @Test
    void findAuthorsByBookCategory_whenCacheMissAndFound_shouldReturnAuthorsFromRepoAndCache() {
        // Arrange
        String category = "Fiction";
        String cacheKey = "authorsByCategory_" + category;
        List<Author> expectedAuthors = List.of(author1);
        when(authorCache.containsKey(cacheKey)).thenReturn(false);
        when(authorRepository.findAuthorsByBookCategory(category)).thenReturn(expectedAuthors);

        // Act
        List<Author> result = authorServiceImpl.findAuthorsByBookCategory(category);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedAuthors, result);
        verify(authorCache, times(1)).containsKey(cacheKey);
        verify(authorCache, never()).get(anyString());
        verify(authorRepository, times(1)).findAuthorsByBookCategory(category);
        verify(authorCache, times(1)).put(cacheKey, expectedAuthors);
    }

    @Test
    void findAuthorsByBookCategory_whenCacheMissAndNotFound_shouldThrowException() {
        // Arrange
        String category = "NonExistent";
        String cacheKey = "authorsByCategory_" + category;
        when(authorCache.containsKey(cacheKey)).thenReturn(false);
        when(authorRepository.findAuthorsByBookCategory(category)).thenReturn(Collections.emptyList());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> authorServiceImpl.findAuthorsByBookCategory(category)
        );
        assertEquals("Авторы не найдены по категории книги: " + category, exception.getMessage());
        verify(authorCache, times(1)).containsKey(cacheKey);
        verify(authorCache, never()).get(anyString());
        verify(authorRepository, times(1)).findAuthorsByBookCategory(category);
        verify(authorCache, never()).put(anyString(), anyList());
    }

    // --- findAuthorsByName ---
    @Test
    void findAuthorsByName_whenCacheHitAndNotEmpty_shouldReturnAuthorsFromCache() {
        // Arrange
        String name = "Author";
        String cacheKey = "authorsByName_" + name;
        List<Author> expectedAuthors = List.of(author1, author2);
        when(authorCache.containsKey(cacheKey)).thenReturn(true);
        when(authorCache.get(cacheKey)).thenReturn(expectedAuthors);

        // Act
        List<Author> result = authorServiceImpl.findAuthorsByName(name);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedAuthors, result);
        verify(authorCache, times(1)).containsKey(cacheKey);
        verify(authorCache, times(1)).get(cacheKey);
        verify(authorRepository, never()).findByAuthorNameContainingIgnoreCase(anyString());
    }

    @Test
    void findAuthorsByName_whenCacheHitButEmpty_shouldThrowException() {
        // Arrange
        String name = "EmptyResultAuthor";
        String cacheKey = "authorsByName_" + name;
        when(authorCache.containsKey(cacheKey)).thenReturn(true);
        when(authorCache.get(cacheKey)).thenReturn(Collections.emptyList()); // Cache has empty list

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> authorServiceImpl.findAuthorsByName(name)
        );
        assertEquals("Авторы не найдены по имени: " + name, exception.getMessage());
        verify(authorCache, times(1)).containsKey(cacheKey);
        verify(authorCache, times(1)).get(cacheKey);
        verify(authorRepository, never()).findByAuthorNameContainingIgnoreCase(anyString());
    }

    @Test
    void findAuthorsByName_whenCacheMissAndFound_shouldReturnAuthorsFromRepoAndCache() {
        // Arrange
        String name = "Author";
        String cacheKey = "authorsByName_" + name;
        List<Author> expectedAuthors = List.of(author1, author2);
        when(authorCache.containsKey(cacheKey)).thenReturn(false);
        when(authorRepository.findByAuthorNameContainingIgnoreCase(name)).thenReturn(expectedAuthors);

        // Act
        List<Author> result = authorServiceImpl.findAuthorsByName(name);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedAuthors, result);
        verify(authorCache, times(1)).containsKey(cacheKey);
        verify(authorCache, never()).get(anyString());
        verify(authorRepository, times(1)).findByAuthorNameContainingIgnoreCase(name);
        verify(authorCache, times(1)).put(cacheKey, expectedAuthors);
    }

    @Test
    void findAuthorsByName_whenCacheMissAndNotFound_shouldThrowException() {
        // Arrange
        String name = "NonExistent";
        String cacheKey = "authorsByName_" + name;
        when(authorCache.containsKey(cacheKey)).thenReturn(false);
        when(authorRepository.findByAuthorNameContainingIgnoreCase(name)).thenReturn(Collections.emptyList());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> authorServiceImpl.findAuthorsByName(name)
        );
        assertEquals("Авторы не найдены по имени: " + name, exception.getMessage());
        verify(authorCache, times(1)).containsKey(cacheKey);
        verify(authorCache, never()).get(anyString());
        verify(authorRepository, times(1)).findByAuthorNameContainingIgnoreCase(name);
        verify(authorCache, never()).put(anyString(), anyList());
    }

    // --- findAuthorsByBookCategoryNative ---
    @Test
    void findAuthorsByBookCategoryNative_whenCacheHit_shouldReturnAuthorsFromCache() {
        // Arrange
        String category = "NativeCategory";
        String cacheKey = "authorsByCategoryNative_" + category;
        List<Author> expectedAuthors = List.of(author1);
        when(authorCache.containsKey(cacheKey)).thenReturn(true);
        when(authorCache.get(cacheKey)).thenReturn(expectedAuthors);

        // Act
        List<Author> result = authorServiceImpl.findAuthorsByBookCategoryNative(category);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedAuthors, result);
        verify(authorCache, times(1)).containsKey(cacheKey);
        verify(authorCache, times(1)).get(cacheKey);
        verify(authorRepository, never()).findAuthorsByBookCategoryNative(anyString());
    }

    @Test
    void findAuthorsByBookCategoryNative_whenCacheMissAndFound_shouldReturnAuthorsFromRepoAndCache() {
        // Arrange
        String category = "NativeCategory";
        String cacheKey = "authorsByCategoryNative_" + category;
        List<Author> expectedAuthors = List.of(author1);
        when(authorCache.containsKey(cacheKey)).thenReturn(false);
        when(authorRepository.findAuthorsByBookCategoryNative(category)).thenReturn(expectedAuthors);

        // Act
        List<Author> result = authorServiceImpl.findAuthorsByBookCategoryNative(category);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedAuthors, result);
        verify(authorCache, times(1)).containsKey(cacheKey);
        verify(authorCache, never()).get(anyString());
        verify(authorRepository, times(1)).findAuthorsByBookCategoryNative(category);
        verify(authorCache, times(1)).put(cacheKey, expectedAuthors);
    }

    @Test
    void findAuthorsByBookCategoryNative_whenCacheMissAndNotFound_shouldReturnEmptyListAndCache() {
        // Arrange
        String category = "NonExistentNative";
        String cacheKey = "authorsByCategoryNative_" + category;
        when(authorCache.containsKey(cacheKey)).thenReturn(false);
        when(authorRepository.findAuthorsByBookCategoryNative(category)).thenReturn(Collections.emptyList());

        // Act
        List<Author> result = authorServiceImpl.findAuthorsByBookCategoryNative(category);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(authorCache, times(1)).containsKey(cacheKey);
        verify(authorCache, never()).get(anyString());
        verify(authorRepository, times(1)).findAuthorsByBookCategoryNative(category);
        verify(authorCache, times(1)).put(cacheKey, Collections.emptyList()); // Cache the empty result
    }

    // --- createAuthor ---
    @Test
    void createAuthor_whenNameIsUnique_shouldCreateAndReturnAuthor() {
        // Arrange
        AuthorCreateDto dto = new AuthorCreateDto();
        dto.setName("New Author");

        Author authorToSave = new Author(); // The object that will be saved
        authorToSave.setAuthorName("New Author");
        authorToSave.setBooks(new ArrayList<>()); // Ensure list is initialized

        Author savedAuthor = new Author(); // The object returned by save
        savedAuthor.setAuthorId(3);
        savedAuthor.setAuthorName("New Author");
        savedAuthor.setBooks(new ArrayList<>());

        when(authorRepository.existsByAuthorName(dto.getName())).thenReturn(false);
        // Use ArgumentCaptor to verify the object passed to save
        ArgumentCaptor<Author> authorCaptor = ArgumentCaptor.forClass(Author.class);
        when(authorRepository.save(authorCaptor.capture())).thenReturn(savedAuthor);

        // Act
        Author result = authorServiceImpl.createAuthor(dto);

        // Assert
        assertNotNull(result);
        assertEquals("New Author", result.getAuthorName());
        assertEquals(3, result.getAuthorId()); // Check the ID from the saved object

        // Verify the captured author details before save
        Author capturedAuthor = authorCaptor.getValue();
        assertEquals("New Author", capturedAuthor.getAuthorName());
        assertNotNull(capturedAuthor.getBooks()); // Check list initialization
        assertTrue(capturedAuthor.getBooks().isEmpty());

        verify(authorRepository, times(1)).existsByAuthorName(dto.getName());
        verify(authorRepository, times(1)).save(any(Author.class));
        verify(authorCache, times(1)).clear();
    }

    @Test
    void createAuthor_whenNameExists_shouldThrowUniqueConstraintViolationException() {
        // Arrange
        AuthorCreateDto dto = new AuthorCreateDto();
        dto.setName("Author One"); // Existing name

        when(authorRepository.existsByAuthorName(dto.getName())).thenReturn(true);

        // Act & Assert
        UniqueConstraintViolationException exception = assertThrows(UniqueConstraintViolationException.class,
                () -> authorServiceImpl.createAuthor(dto)
        );
        assertEquals("Автор с таким именем уже существует", exception.getMessage());

        verify(authorRepository, times(1)).existsByAuthorName(dto.getName());
        verify(authorRepository, never()).save(any(Author.class));
        verify(authorCache, never()).clear();
    }

    // --- updateAuthor (Parameterized) ---

    // Provider method for update scenarios
    private static Stream<Arguments> updateAuthorNameScenarios() {
        return Stream.of(
                Arguments.of("Updated Author One", "Updated Author One"), // Valid name update
                Arguments.of(null, "Author One"),                       // Null name, should not update
                Arguments.of("   ", "Author One")                        // Blank name, should not update
        );
    }

    @ParameterizedTest(name = "Input Name: \"{0}\", Expected Name: \"{1}\"")
    @MethodSource("updateAuthorNameScenarios")
    void updateAuthor_whenAuthorExists_handlesNameUpdateCorrectly(String inputName, String expectedName) {
        // Arrange
        int authorId = 1;
        AuthorUpdateDto dto = new AuthorUpdateDto();
        dto.setAuthorName(inputName);

        Author originalAuthor = new Author();
        originalAuthor.setAuthorId(authorId);
        originalAuthor.setAuthorName("Author One"); // Initial name before update

        when(authorRepository.findById(authorId)).thenReturn(Optional.of(originalAuthor));
        when(authorRepository.save(any(Author.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Author result = authorServiceImpl.updateAuthor(authorId, dto);

        // Assert
        assertNotNull(result);
        assertEquals(authorId, result.getAuthorId());
        assertEquals(expectedName, result.getAuthorName()); // Verify if the name was updated as expected

        verify(authorRepository, times(1)).findById(authorId);
        verify(authorRepository, times(1)).save(originalAuthor); // save called on the found object
        verify(authorCache, times(1)).clear();
    }


    @Test
    void updateAuthor_whenAuthorNotFound_shouldThrowEntityNotFoundException() {
        // Arrange
        int authorId = 99;
        AuthorUpdateDto dto = new AuthorUpdateDto();
        dto.setAuthorName("Update Attempt");

        when(authorRepository.findById(authorId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> authorServiceImpl.updateAuthor(authorId, dto)
        );
        assertEquals("Автор не найден с id: " + authorId, exception.getMessage());

        verify(authorRepository, times(1)).findById(authorId);
        verify(authorRepository, never()).save(any(Author.class));
        verify(authorCache, never()).clear();
    }

    // --- deleteAuthor (Parameterized) ---

    // Provider method for delete scenarios
    private static Stream<Arguments> deleteAuthorScenarios() {
        // Scenario 1: Author with books
        Author authorWithBooks = new Author();
        authorWithBooks.setAuthorId(10); // Use distinct IDs for clarity
        authorWithBooks.setAuthorName("Author With Books");
        Book bookForThisAuthor = new Book();
        bookForThisAuthor.setBookId(101);
        bookForThisAuthor.setBookName("Book For Deletion Test");
        authorWithBooks.setBooks(new ArrayList<>(List.of(bookForThisAuthor)));
        bookForThisAuthor.setAuthor(authorWithBooks); // Set bidirectional link

        // Scenario 2: Author with null books list
        Author authorWithNullList = new Author();
        authorWithNullList.setAuthorId(11);
        authorWithNullList.setAuthorName("Author NullBooks");
        authorWithNullList.setBooks(null); // Explicitly null

        // Scenario 3: Author with empty books list
        Author authorWithEmptyList = new Author();
        authorWithEmptyList.setAuthorId(12);
        authorWithEmptyList.setAuthorName("Author EmptyBooks");
        authorWithEmptyList.setBooks(new ArrayList<>()); // Explicitly empty

        return Stream.of(
                Arguments.of("With Books", authorWithBooks, bookForThisAuthor),
                Arguments.of("Null List", authorWithNullList, null),
                Arguments.of("Empty List", authorWithEmptyList, null)
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("deleteAuthorScenarios")
    void deleteAuthor_handlesDifferentBookListStates(String ignoredDescription, Author authorToDelete, Book bookToCheck) {
        // Arrange
        int authorId = authorToDelete.getAuthorId();
        when(authorRepository.findById(authorId)).thenReturn(Optional.of(authorToDelete));
        doNothing().when(authorRepository).delete(authorToDelete);

        // Act
        authorServiceImpl.deleteAuthor(authorId);

        // Assert
        verify(authorRepository, times(1)).findById(authorId);
        verify(authorRepository, times(1)).delete(authorToDelete);
        verify(authorCache, times(1)).clear();

        // Verify book nullification only if a book was provided for checking
        if (bookToCheck != null) {
            assertNull(bookToCheck.getAuthor(), "Book's author reference should be null after deleting author");
        }
        // The main assertion is that no NullPointerException occurred during the service call for null/empty lists.
    }

    @Test
    void deleteAuthor_whenAuthorNotFound_shouldThrowEntityNotFoundException() {
        // Arrange
        int authorId = 99;
        when(authorRepository.findById(authorId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> authorServiceImpl.deleteAuthor(authorId)
        );
        assertEquals("Автор не найден с id: " + authorId, exception.getMessage());

        verify(authorRepository, times(1)).findById(authorId);
        verify(authorRepository, never()).delete(any(Author.class));
        verify(authorCache, never()).clear();
    }
}