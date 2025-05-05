
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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


    private Author author1;
    private Author author2;

    @BeforeEach
    void setUp() {

        author1 = new Author();
        author1.setAuthorId(1);
        author1.setAuthorName("Author One");
        author1.setBooks(new ArrayList<>());

        author2 = new Author();
        author2.setAuthorId(2);
        author2.setAuthorName("Author Two");
        author2.setBooks(new ArrayList<>());


        Book bookForAuthor1 = new Book();
        bookForAuthor1.setBookId(1);
        bookForAuthor1.setBookName("Book by Author One");
        bookForAuthor1.setAuthor(author1);
        author1.getBooks().add(bookForAuthor1);
    }


    @Test
    void findAllAuthors_shouldReturnAllAuthors() {

        when(authorRepository.findAllWithBooks()).thenReturn(List.of(author1, author2));


        List<Author> result = authorServiceImpl.findAllAuthors();


        assertNotNull(result);
        assertEquals(2, result.size());
        verify(authorRepository, times(1)).findAllWithBooks();
        verifyNoInteractions(authorCache);
    }


    @Test
    void findAuthorById_whenCacheHit_shouldReturnAuthorFromCache() {

        int authorId = 1;
        String cacheKey = "author_id_" + authorId;
        when(authorCache.containsKey(cacheKey)).thenReturn(true);
        when(authorCache.get(cacheKey)).thenReturn(List.of(author1));


        Author result = authorServiceImpl.findAuthorById(authorId);


        assertNotNull(result);
        assertEquals(authorId, result.getAuthorId());
        verify(authorCache, times(1)).containsKey(cacheKey);
        verify(authorCache, times(1)).get(cacheKey);
        verify(authorRepository, never()).findById(anyInt());
    }

    @Test
    void findAuthorById_whenCacheMissAndFound_shouldReturnAuthorFromRepoAndCache() {

        int authorId = 1;
        String cacheKey = "author_id_" + authorId;
        when(authorCache.containsKey(cacheKey)).thenReturn(false);
        when(authorRepository.findById(authorId)).thenReturn(Optional.of(author1));


        Author result = authorServiceImpl.findAuthorById(authorId);


        assertNotNull(result);
        assertEquals(authorId, result.getAuthorId());
        verify(authorCache, times(1)).containsKey(cacheKey);
        verify(authorCache, never()).get(anyString());
        verify(authorRepository, times(1)).findById(authorId);
        verify(authorCache, times(1)).put(cacheKey, List.of(author1));
    }

    @Test
    void findAuthorById_whenCacheMissAndNotFound_shouldThrowException() {

        int authorId = 99;
        String cacheKey = "author_id_" + authorId;
        when(authorCache.containsKey(cacheKey)).thenReturn(false);
        when(authorRepository.findById(authorId)).thenReturn(Optional.empty());


        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> authorServiceImpl.findAuthorById(authorId)
        );
        assertEquals("Автор не найден с id: " + authorId, exception.getMessage());
        verify(authorCache, times(1)).containsKey(cacheKey);
        verify(authorCache, never()).get(anyString());
        verify(authorRepository, times(1)).findById(authorId);
        verify(authorCache, never()).put(anyString(), anyList());
    }


    @Test
    void findAuthorsByBookCategory_whenCacheHitAndNotEmpty_shouldReturnAuthorsFromCache() {

        String category = "Fiction";
        String cacheKey = "authorsByCategory_" + category;
        List<Author> expectedAuthors = List.of(author1);
        when(authorCache.containsKey(cacheKey)).thenReturn(true);
        when(authorCache.get(cacheKey)).thenReturn(expectedAuthors);


        List<Author> result = authorServiceImpl.findAuthorsByBookCategory(category);


        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedAuthors, result);
        verify(authorCache, times(1)).containsKey(cacheKey);
        verify(authorCache, times(1)).get(cacheKey);
        verify(authorRepository, never()).findAuthorsByBookCategory(anyString());
    }

    @Test
    void findAuthorsByBookCategory_whenCacheHitButEmpty_shouldThrowException() {

        String category = "EmptyCategory";
        String cacheKey = "authorsByCategory_" + category;
        when(authorCache.containsKey(cacheKey)).thenReturn(true);
        when(authorCache.get(cacheKey)).thenReturn(Collections.emptyList());


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

        String category = "Fiction";
        String cacheKey = "authorsByCategory_" + category;
        List<Author> expectedAuthors = List.of(author1);
        when(authorCache.containsKey(cacheKey)).thenReturn(false);
        when(authorRepository.findAuthorsByBookCategory(category)).thenReturn(expectedAuthors);


        List<Author> result = authorServiceImpl.findAuthorsByBookCategory(category);


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

        String category = "NonExistent";
        String cacheKey = "authorsByCategory_" + category;
        when(authorCache.containsKey(cacheKey)).thenReturn(false);
        when(authorRepository.findAuthorsByBookCategory(category)).thenReturn(Collections.emptyList());


        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> authorServiceImpl.findAuthorsByBookCategory(category)
        );
        assertEquals("Авторы не найдены по категории книги: " + category, exception.getMessage());
        verify(authorCache, times(1)).containsKey(cacheKey);
        verify(authorCache, never()).get(anyString());
        verify(authorRepository, times(1)).findAuthorsByBookCategory(category);
        verify(authorCache, never()).put(anyString(), anyList());
    }


    @Test
    void findAuthorsByName_whenCacheHitAndNotEmpty_shouldReturnAuthorsFromCache() {

        String name = "Author";
        String cacheKey = "authorsByName_" + name;
        List<Author> expectedAuthors = List.of(author1, author2);
        when(authorCache.containsKey(cacheKey)).thenReturn(true);
        when(authorCache.get(cacheKey)).thenReturn(expectedAuthors);


        List<Author> result = authorServiceImpl.findAuthorsByName(name);


        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedAuthors, result);
        verify(authorCache, times(1)).containsKey(cacheKey);
        verify(authorCache, times(1)).get(cacheKey);
        verify(authorRepository, never()).findByAuthorNameContainingIgnoreCase(anyString());
    }

    @Test
    void findAuthorsByName_whenCacheHitButEmpty_shouldThrowException() {

        String name = "EmptyResultAuthor";
        String cacheKey = "authorsByName_" + name;
        when(authorCache.containsKey(cacheKey)).thenReturn(true);
        when(authorCache.get(cacheKey)).thenReturn(Collections.emptyList());


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

        String name = "Author";
        String cacheKey = "authorsByName_" + name;
        List<Author> expectedAuthors = List.of(author1, author2);
        when(authorCache.containsKey(cacheKey)).thenReturn(false);
        when(authorRepository.findByAuthorNameContainingIgnoreCase(name)).thenReturn(expectedAuthors);


        List<Author> result = authorServiceImpl.findAuthorsByName(name);


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

        String name = "NonExistent";
        String cacheKey = "authorsByName_" + name;
        when(authorCache.containsKey(cacheKey)).thenReturn(false);
        when(authorRepository.findByAuthorNameContainingIgnoreCase(name)).thenReturn(Collections.emptyList());


        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> authorServiceImpl.findAuthorsByName(name)
        );
        assertEquals("Авторы не найдены по имени: " + name, exception.getMessage());
        verify(authorCache, times(1)).containsKey(cacheKey);
        verify(authorCache, never()).get(anyString());
        verify(authorRepository, times(1)).findByAuthorNameContainingIgnoreCase(name);
        verify(authorCache, never()).put(anyString(), anyList());
    }


    @Test
    void findAuthorsByBookCategoryNative_whenCacheHit_shouldReturnAuthorsFromCache() {

        String category = "NativeCategory";
        String cacheKey = "authorsByCategoryNative_" + category;
        List<Author> expectedAuthors = List.of(author1);
        when(authorCache.containsKey(cacheKey)).thenReturn(true);
        when(authorCache.get(cacheKey)).thenReturn(expectedAuthors);


        List<Author> result = authorServiceImpl.findAuthorsByBookCategoryNative(category);


        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedAuthors, result);
        verify(authorCache, times(1)).containsKey(cacheKey);
        verify(authorCache, times(1)).get(cacheKey);
        verify(authorRepository, never()).findAuthorsByBookCategoryNative(anyString());
    }

    @Test
    void findAuthorsByBookCategoryNative_whenCacheMissAndFound_shouldReturnAuthorsFromRepoAndCache() {

        String category = "NativeCategory";
        String cacheKey = "authorsByCategoryNative_" + category;
        List<Author> expectedAuthors = List.of(author1);
        when(authorCache.containsKey(cacheKey)).thenReturn(false);
        when(authorRepository.findAuthorsByBookCategoryNative(category)).thenReturn(expectedAuthors);


        List<Author> result = authorServiceImpl.findAuthorsByBookCategoryNative(category);


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

        String category = "NonExistentNative";
        String cacheKey = "authorsByCategoryNative_" + category;
        when(authorCache.containsKey(cacheKey)).thenReturn(false);
        when(authorRepository.findAuthorsByBookCategoryNative(category)).thenReturn(Collections.emptyList());


        List<Author> result = authorServiceImpl.findAuthorsByBookCategoryNative(category);


        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(authorCache, times(1)).containsKey(cacheKey);
        verify(authorCache, never()).get(anyString());
        verify(authorRepository, times(1)).findAuthorsByBookCategoryNative(category);
        verify(authorCache, times(1)).put(cacheKey, Collections.emptyList());
    }


    @Test
    void createAuthor_whenNameIsUnique_shouldCreateAndReturnAuthor() {

        AuthorCreateDto dto = new AuthorCreateDto();
        dto.setName("New Author");

        Author authorToSave = new Author();
        authorToSave.setAuthorName("New Author");
        authorToSave.setBooks(new ArrayList<>());

        Author savedAuthor = new Author();
        savedAuthor.setAuthorId(3);
        savedAuthor.setAuthorName("New Author");
        savedAuthor.setBooks(new ArrayList<>());

        when(authorRepository.existsByAuthorName(dto.getName())).thenReturn(false);

        ArgumentCaptor<Author> authorCaptor = ArgumentCaptor.forClass(Author.class);
        when(authorRepository.save(authorCaptor.capture())).thenReturn(savedAuthor);


        Author result = authorServiceImpl.createAuthor(dto);


        assertNotNull(result);
        assertEquals("New Author", result.getAuthorName());
        assertEquals(3, result.getAuthorId());


        Author capturedAuthor = authorCaptor.getValue();
        assertEquals("New Author", capturedAuthor.getAuthorName());
        assertNotNull(capturedAuthor.getBooks());
        assertTrue(capturedAuthor.getBooks().isEmpty());

        verify(authorRepository, times(1)).existsByAuthorName(dto.getName());
        verify(authorRepository, times(1)).save(any(Author.class));
        verify(authorCache, times(1)).clear();
    }

    @Test
    void createAuthor_whenNameExists_shouldThrowUniqueConstraintViolationException() {

        AuthorCreateDto dto = new AuthorCreateDto();
        dto.setName("Author One");

        when(authorRepository.existsByAuthorName(dto.getName())).thenReturn(true);


        UniqueConstraintViolationException exception = assertThrows(UniqueConstraintViolationException.class,
                () -> authorServiceImpl.createAuthor(dto)
        );
        assertEquals("Автор с таким именем уже существует", exception.getMessage());

        verify(authorRepository, times(1)).existsByAuthorName(dto.getName());
        verify(authorRepository, never()).save(any(Author.class));
        verify(authorCache, never()).clear();
    }




    private static Stream<Arguments> updateAuthorNameScenarios() {
        return Stream.of(
                Arguments.of("Updated Author One", "Updated Author One"),
                Arguments.of(null, "Author One"),
                Arguments.of("   ", "Author One")
        );
    }

    @ParameterizedTest(name = "Input Name: \"{0}\", Expected Name: \"{1}\"")
    @MethodSource("updateAuthorNameScenarios")
    void updateAuthor_whenAuthorExists_handlesNameUpdateCorrectly(String inputName, String expectedName) {

        int authorId = 1;
        AuthorUpdateDto dto = new AuthorUpdateDto();
        dto.setAuthorName(inputName);

        Author originalAuthor = new Author();
        originalAuthor.setAuthorId(authorId);
        originalAuthor.setAuthorName("Author One");

        when(authorRepository.findById(authorId)).thenReturn(Optional.of(originalAuthor));
        when(authorRepository.save(any(Author.class))).thenAnswer(invocation -> invocation.getArgument(0));


        Author result = authorServiceImpl.updateAuthor(authorId, dto);


        assertNotNull(result);
        assertEquals(authorId, result.getAuthorId());
        assertEquals(expectedName, result.getAuthorName());

        verify(authorRepository, times(1)).findById(authorId);
        verify(authorRepository, times(1)).save(originalAuthor);
        verify(authorCache, times(1)).clear();
    }


    @Test
    void updateAuthor_whenAuthorNotFound_shouldThrowEntityNotFoundException() {

        int authorId = 99;
        AuthorUpdateDto dto = new AuthorUpdateDto();
        dto.setAuthorName("Update Attempt");

        when(authorRepository.findById(authorId)).thenReturn(Optional.empty());


        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> authorServiceImpl.updateAuthor(authorId, dto)
        );
        assertEquals("Автор не найден с id: " + authorId, exception.getMessage());

        verify(authorRepository, times(1)).findById(authorId);
        verify(authorRepository, never()).save(any(Author.class));
        verify(authorCache, never()).clear();
    }




    private static Stream<Arguments> deleteAuthorScenarios() {

        Author authorWithBooks = new Author();
        authorWithBooks.setAuthorId(10);
        authorWithBooks.setAuthorName("Author With Books");
        Book bookForThisAuthor = new Book();
        bookForThisAuthor.setBookId(101);
        bookForThisAuthor.setBookName("Book For Deletion Test");
        authorWithBooks.setBooks(new ArrayList<>(List.of(bookForThisAuthor)));
        bookForThisAuthor.setAuthor(authorWithBooks);


        Author authorWithNullList = new Author();
        authorWithNullList.setAuthorId(11);
        authorWithNullList.setAuthorName("Author NullBooks");
        authorWithNullList.setBooks(null);


        Author authorWithEmptyList = new Author();
        authorWithEmptyList.setAuthorId(12);
        authorWithEmptyList.setAuthorName("Author EmptyBooks");
        authorWithEmptyList.setBooks(new ArrayList<>());

        return Stream.of(
                Arguments.of("With Books", authorWithBooks, bookForThisAuthor),
                Arguments.of("Null List", authorWithNullList, null),
                Arguments.of("Empty List", authorWithEmptyList, null)
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("deleteAuthorScenarios")
    void deleteAuthor_handlesDifferentBookListStates(String ignoredDescription, Author authorToDelete, Book bookToCheck) {

        int authorId = authorToDelete.getAuthorId();
        when(authorRepository.findById(authorId)).thenReturn(Optional.of(authorToDelete));
        doNothing().when(authorRepository).delete(authorToDelete);


        authorServiceImpl.deleteAuthor(authorId);


        verify(authorRepository, times(1)).findById(authorId);
        verify(authorRepository, times(1)).delete(authorToDelete);
        verify(authorCache, times(1)).clear();


        if (bookToCheck != null) {
            assertNull(bookToCheck.getAuthor(), "Book's author reference should be null after deleting author");
        }

    }

    @Test
    void deleteAuthor_whenAuthorNotFound_shouldThrowEntityNotFoundException() {

        int authorId = 99;
        when(authorRepository.findById(authorId)).thenReturn(Optional.empty());


        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> authorServiceImpl.deleteAuthor(authorId)
        );
        assertEquals("Автор не найден с id: " + authorId, exception.getMessage());

        verify(authorRepository, times(1)).findById(authorId);
        verify(authorRepository, never()).delete(any(Author.class));
        verify(authorCache, never()).clear();
    }
}