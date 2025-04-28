
package com.example.myspringproject.service.impl;

import com.example.myspringproject.cache.CategoryCache;
import com.example.myspringproject.dto.create.CategoryCreateDto;
import com.example.myspringproject.dto.update.CategoryUpdateDto;
import com.example.myspringproject.exception.UniqueConstraintViolationException;
import com.example.myspringproject.model.Book;
import com.example.myspringproject.model.Category;
import com.example.myspringproject.repository.BookRepository;
import com.example.myspringproject.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private CategoryCache categoryCache;

    @InjectMocks
    private CategoryServiceImpl categoryServiceImpl;

    private Category category1;
    private Category category2;
    private Book book1;
    private Book book2;

    @BeforeEach
    void setUp() {
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
        book1.setBookName("Book One");
        book1.setCategories(new ArrayList<>(List.of(category1)));
        category1.getBooks().add(book1);

        book2 = new Book();
        book2.setBookId(2);
        book2.setBookName("Book Two");
        book2.setCategories(new ArrayList<>(List.of(category1, category2)));
        category1.getBooks().add(book2);
        category2.getBooks().add(book2);
    }

    @Test
    void getAllCategories_shouldReturnAllCategories() {

        when(categoryRepository.findAll()).thenReturn(List.of(category1, category2));


        List<Category> result = categoryServiceImpl.getAllCategories();


        assertNotNull(result);
        assertEquals(2, result.size());
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    void getCategoryById_whenCacheHit_shouldReturnCategoryFromCache() {

        int categoryId = 1;
        String cacheKey = "category_id_" + categoryId;
        when(categoryCache.containsKey(cacheKey)).thenReturn(true);
        when(categoryCache.get(cacheKey)).thenReturn(List.of(category1));


        Category result = categoryServiceImpl.getCategoryById(categoryId);


        assertNotNull(result);
        assertEquals(categoryId, result.getCategoryId());
        verify(categoryCache, times(1)).containsKey(cacheKey);
        verify(categoryCache, times(1)).get(cacheKey);
        verify(categoryRepository, never()).findById(anyInt());
    }

    @Test
    void getCategoryById_whenCacheMissAndFound_shouldReturnCategoryFromRepoAndCache() {

        int categoryId = 1;
        String cacheKey = "category_id_" + categoryId;
        when(categoryCache.containsKey(cacheKey)).thenReturn(false);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category1));


        Category result = categoryServiceImpl.getCategoryById(categoryId);


        assertNotNull(result);
        assertEquals(categoryId, result.getCategoryId());
        verify(categoryCache, times(1)).containsKey(cacheKey);
        verify(categoryCache, never()).get(anyString());
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryCache, times(1)).put(cacheKey, List.of(category1));
    }

    @Test
    void getCategoryById_whenCacheMissAndNotFound_shouldThrowException() {

        int categoryId = 99;
        String cacheKey = "category_id_" + categoryId;
        when(categoryCache.containsKey(cacheKey)).thenReturn(false);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());


        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> categoryServiceImpl.getCategoryById(categoryId));
        assertEquals("Категория не найдена по id:" + categoryId, exception.getMessage());
        verify(categoryCache, times(1)).containsKey(cacheKey);
        verify(categoryCache, never()).get(anyString());
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryCache, never()).put(anyString(), anyList());
    }

    @Test
    void findCategoriesByBookId_whenBookExistsAndCacheMiss_shouldReturnCategories() {
        int bookId = 2;
        String cacheKey = "categoriesByBookId_" + bookId;
        when(bookRepository.existsById(bookId)).thenReturn(true);
        when(categoryCache.containsKey(cacheKey)).thenReturn(false);
        when(categoryRepository.findCategoriesByBookId(bookId)).thenReturn(List.of(category1, category2));

        List<Category> result = categoryServiceImpl.findCategoriesByBookId(bookId);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bookRepository, times(1)).existsById(bookId);
        verify(categoryCache, times(1)).containsKey(cacheKey);
        verify(categoryRepository, times(1)).findCategoriesByBookId(bookId);
        verify(categoryCache, times(1)).put(cacheKey, List.of(category1, category2));
    }

    @Test
    void findCategoriesByBookId_whenBookNotFound_shouldThrowException() {
        int bookId = 99;
        when(bookRepository.existsById(bookId)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> categoryServiceImpl.findCategoriesByBookId(bookId));

        assertEquals("Книга не найдена с id: " + bookId, exception.getMessage());
        verify(bookRepository, times(1)).existsById(bookId);
        verify(categoryCache, never()).containsKey(anyString());
        verify(categoryRepository, never()).findCategoriesByBookId(anyInt());
    }


    @Test
    void createCategory_whenValidDtoWithoutBooks_shouldCreateAndReturnCategory() {

        CategoryCreateDto dto = new CategoryCreateDto();
        dto.setName("New Category");


        Category newCategory = new Category();
        newCategory.setCategoryId(3);
        newCategory.setCategoryName("New Category");

        when(categoryRepository.existsByCategoryName(dto.getName())).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(newCategory);


        Category result = categoryServiceImpl.createCategory(dto);


        assertNotNull(result);
        assertEquals("New Category", result.getCategoryName());
        assertEquals(3, result.getCategoryId());
        assertNull(result.getBooks());
        verify(categoryRepository, times(1)).existsByCategoryName(dto.getName());
        verify(bookRepository, never()).findAllById(anyList());
        verify(categoryRepository, times(1)).save(any(Category.class));
        verify(categoryCache, times(1)).clear();
    }

    @Test
    void createCategory_whenValidDtoWithBooks_shouldCreateAndReturnCategory() {

        CategoryCreateDto dto = new CategoryCreateDto();
        dto.setName("New Category With Books");
        dto.setBookIds(List.of(1, 2));

        Category newCategory = new Category();
        newCategory.setCategoryId(4);
        newCategory.setCategoryName("New Category With Books");

        newCategory.setBooks(new ArrayList<>());

        when(categoryRepository.existsByCategoryName(dto.getName())).thenReturn(false);
        when(bookRepository.findAllById(dto.getBookIds())).thenReturn(List.of(book1, book2));

        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category savedCategory = invocation.getArgument(0);
            savedCategory.setCategoryId(4);

            if (savedCategory.getBooks() != null) {
                savedCategory.getBooks().forEach(book -> {
                    if (book.getCategories() == null) book.setCategories(new ArrayList<>());
                    if (!book.getCategories().contains(savedCategory)) {
                        book.getCategories().add(savedCategory);
                    }
                });
            }
            return savedCategory;
        });



        Category result = categoryServiceImpl.createCategory(dto);


        assertNotNull(result);
        assertEquals("New Category With Books", result.getCategoryName());
        assertEquals(4, result.getCategoryId());

        assertNotNull(result.getBooks());
        assertEquals(2, result.getBooks().size());
        assertTrue(result.getBooks().contains(book1));
        assertTrue(result.getBooks().contains(book2));

        verify(categoryRepository, times(1)).existsByCategoryName(dto.getName());
        verify(bookRepository, times(1)).findAllById(dto.getBookIds());
        verify(categoryRepository, times(1)).save(any(Category.class));
        verify(categoryCache, times(1)).clear();

        assertTrue(book1.getCategories().stream().anyMatch(c -> c.getCategoryId() == 4));
        assertTrue(book2.getCategories().stream().anyMatch(c -> c.getCategoryId() == 4));
    }

    @Test
    void createCategory_whenNameExists_shouldThrowUniqueConstraintViolationException() {

        CategoryCreateDto dto = new CategoryCreateDto();
        dto.setName("Fiction");

        when(categoryRepository.existsByCategoryName(dto.getName())).thenReturn(true);


        UniqueConstraintViolationException exception = assertThrows(UniqueConstraintViolationException.class, () -> categoryServiceImpl.createCategory(dto));
        assertEquals("Категория с таким именем уже существует", exception.getMessage());

        verify(categoryRepository, times(1)).existsByCategoryName(dto.getName());
        verify(bookRepository, never()).findAllById(anyList());
        verify(categoryRepository, never()).save(any(Category.class));
        verify(categoryCache, never()).clear();
    }

    @Test
    void createCategory_whenBookNotFound_shouldThrowIllegalArgumentException() {

        CategoryCreateDto dto = new CategoryCreateDto();
        dto.setName("Category With Missing Book");
        dto.setBookIds(List.of(1, 99));

        when(categoryRepository.existsByCategoryName(dto.getName())).thenReturn(false);
        when(bookRepository.findAllById(dto.getBookIds())).thenReturn(List.of(book1));


        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> categoryServiceImpl.createCategory(dto));
        assertEquals("Some books not found", exception.getMessage());

        verify(categoryRepository, times(1)).existsByCategoryName(dto.getName());
        verify(bookRepository, times(1)).findAllById(dto.getBookIds());
        verify(categoryRepository, never()).save(any(Category.class));
        verify(categoryCache, never()).clear();
    }

    @Test
    void updateCategory_whenCategoryExists_shouldUpdateAndReturnCategory() {

        int categoryId = 1;
        CategoryUpdateDto dto = new CategoryUpdateDto();
        dto.setName("Updated Fiction");
        dto.setBookIds(List.of(2));


        if (book2.getCategories() == null) {
            book2.setCategories(new ArrayList<>());
        }

        if (category1.getBooks() == null) {
            category1.setBooks(new ArrayList<>());
        }


        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category1));
        when(bookRepository.findAllById(dto.getBookIds())).thenReturn(List.of(book2));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {

            Category catToSave = invocation.getArgument(0);
            List<Book> newBooks = catToSave.getBooks();
            if (newBooks != null) {
                newBooks.forEach(book -> {
                    if (book.getCategories() == null) book.setCategories(new ArrayList<>());
                    if (!book.getCategories().contains(catToSave)) {
                        book.getCategories().add(catToSave);
                    }
                });
            }
            return catToSave;
        });


        Category result = categoryServiceImpl.updateCategory(categoryId, dto);


        assertNotNull(result);
        assertEquals(categoryId, result.getCategoryId());
        assertEquals("Updated Fiction", result.getCategoryName());
        assertNotNull(result.getBooks());
        assertEquals(1, result.getBooks().size());
        assertTrue(result.getBooks().contains(book2));

        assertTrue(book2.getCategories().contains(category1));

        verify(categoryRepository, times(1)).findById(categoryId);
        verify(bookRepository, times(1)).findAllById(dto.getBookIds());
        verify(categoryRepository, times(1)).save(category1);
        verify(categoryCache, times(1)).clear();
    }

    @Test
    void updateCategory_whenCategoryNotFound_shouldThrowEntityNotFoundException() {

        int categoryId = 99;
        CategoryUpdateDto dto = new CategoryUpdateDto();
        dto.setName("Update Attempt");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());


        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> categoryServiceImpl.updateCategory(categoryId, dto));
        assertEquals("Category not found", exception.getMessage());

        verify(categoryRepository, times(1)).findById(categoryId);
        verify(bookRepository, never()).findAllById(anyList());
        verify(categoryRepository, never()).save(any(Category.class));
        verify(categoryCache, never()).clear();
    }

    @Test
    void deleteCategory_whenCategoryExists_shouldDeleteCategoryAndRemoveFromBooks() {

        int categoryId = 1;

        if (book1.getCategories() == null) book1.setCategories(new ArrayList<>());
        if (book2.getCategories() == null) book2.setCategories(new ArrayList<>());
        book1.getCategories().add(category1);
        book2.getCategories().add(category1);

        assertTrue(book1.getCategories().contains(category1));
        assertTrue(book2.getCategories().contains(category1));

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category1));
        doNothing().when(categoryRepository).delete(category1);


        categoryServiceImpl.deleteCategory(categoryId);


        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, times(1)).delete(category1);
        verify(categoryCache, times(1)).clear();

        assertFalse(book1.getCategories().contains(category1));
        assertFalse(book2.getCategories().contains(category1));

        assertTrue(book2.getCategories().contains(category2));
    }

    @Test
    void deleteCategory_whenCategoryNotFound_shouldThrowEntityNotFoundException() {

        int categoryId = 99;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());


        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> categoryServiceImpl.deleteCategory(categoryId));
        assertEquals("Category not found", exception.getMessage());

        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, never()).delete(any(Category.class));
        verify(categoryCache, never()).clear();
    }



    @Test
    void findCategoriesByName_whenCacheHit_shouldReturnCategoriesFromCache() {

        String name = "Fi";
        String cacheKey = "categoriesByName_" + name;
        List<Category> expectedCategories = List.of(category1, category2);
        when(categoryCache.containsKey(cacheKey)).thenReturn(true);
        when(categoryCache.get(cacheKey)).thenReturn(expectedCategories);


        List<Category> result = categoryServiceImpl.findCategoriesByName(name);


        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedCategories, result);
        verify(categoryCache, times(1)).containsKey(cacheKey);
        verify(categoryCache, times(1)).get(cacheKey);
        verify(categoryRepository, never()).findByCategoryNameContainingIgnoreCase(anyString());
    }

    @Test
    void findCategoriesByName_whenCacheMissAndFound_shouldReturnCategoriesFromRepoAndCache() {

        String name = "Fiction";
        String cacheKey = "categoriesByName_" + name;
        List<Category> expectedCategories = List.of(category1);
        when(categoryCache.containsKey(cacheKey)).thenReturn(false);
        when(categoryRepository.findByCategoryNameContainingIgnoreCase(name)).thenReturn(expectedCategories);


        List<Category> result = categoryServiceImpl.findCategoriesByName(name);


        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedCategories, result);
        verify(categoryCache, times(1)).containsKey(cacheKey);
        verify(categoryCache, never()).get(anyString());
        verify(categoryRepository, times(1)).findByCategoryNameContainingIgnoreCase(name);
        verify(categoryCache, times(1)).put(cacheKey, expectedCategories);
    }

    @Test
    void findCategoriesByName_whenCacheMissAndNotFound_shouldThrowException() {

        String name = "NonExistent";
        String cacheKey = "categoriesByName_" + name;
        when(categoryCache.containsKey(cacheKey)).thenReturn(false);
        when(categoryRepository.findByCategoryNameContainingIgnoreCase(name)).thenReturn(Collections.emptyList());


        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> categoryServiceImpl.findCategoriesByName(name));
        assertEquals("Категории не найдены по имени: " + name, exception.getMessage());
        verify(categoryCache, times(1)).containsKey(cacheKey);
        verify(categoryCache, never()).get(anyString());
        verify(categoryRepository, times(1)).findByCategoryNameContainingIgnoreCase(name);
        verify(categoryCache, never()).put(anyString(), anyList());
    }

    @Test
    void findCategoriesByName_whenCacheHitButEmpty_shouldThrowException() {

        String name = "EmptyResultCategory";
        String cacheKey = "categoriesByName_" + name;
        when(categoryCache.containsKey(cacheKey)).thenReturn(true);
        when(categoryCache.get(cacheKey)).thenReturn(Collections.emptyList());


        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> categoryServiceImpl.findCategoriesByName(name));
        assertEquals("Категории не найдены по имени: " + name, exception.getMessage());
        verify(categoryCache, times(1)).containsKey(cacheKey);
        verify(categoryCache, times(1)).get(cacheKey);
        verify(categoryRepository, never()).findByCategoryNameContainingIgnoreCase(anyString());
    }




    @Test
    void findCategoriesByBook_whenCacheHit_shouldReturnCategoriesFromCache() {

        String bookName = "Book Two";
        String cacheKey = "categoriesByBook_" + bookName;
        List<Category> expectedCategories = List.of(category1, category2);
        when(categoryCache.containsKey(cacheKey)).thenReturn(true);
        when(categoryCache.get(cacheKey)).thenReturn(expectedCategories);


        List<Category> result = categoryServiceImpl.findCategoriesByBook(bookName);


        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedCategories, result);
        verify(categoryCache, times(1)).containsKey(cacheKey);
        verify(categoryCache, times(1)).get(cacheKey);
        verify(categoryRepository, never()).findCategoriesByBook(anyString());
    }

    @Test
    void findCategoriesByBook_whenCacheMissAndFound_shouldReturnCategoriesFromRepoAndCache() {

        String bookName = "Book Two";
        String cacheKey = "categoriesByBook_" + bookName;
        List<Category> expectedCategories = List.of(category1, category2);
        when(categoryCache.containsKey(cacheKey)).thenReturn(false);
        when(categoryRepository.findCategoriesByBook(bookName)).thenReturn(expectedCategories);


        List<Category> result = categoryServiceImpl.findCategoriesByBook(bookName);


        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedCategories, result);
        verify(categoryCache, times(1)).containsKey(cacheKey);
        verify(categoryCache, never()).get(anyString());
        verify(categoryRepository, times(1)).findCategoriesByBook(bookName);
        verify(categoryCache, times(1)).put(cacheKey, expectedCategories);
    }

    @Test
    void findCategoriesByBook_whenCacheMissAndNotFound_shouldThrowException() {

        String bookName = "NonExistent Book";
        String cacheKey = "categoriesByBook_" + bookName;
        when(categoryCache.containsKey(cacheKey)).thenReturn(false);
        when(categoryRepository.findCategoriesByBook(bookName)).thenReturn(Collections.emptyList());


        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> categoryServiceImpl.findCategoriesByBook(bookName));
        assertEquals("Категории не найдены по книге: " + bookName, exception.getMessage());
        verify(categoryCache, times(1)).containsKey(cacheKey);
        verify(categoryCache, never()).get(anyString());
        verify(categoryRepository, times(1)).findCategoriesByBook(bookName);
        verify(categoryCache, never()).put(anyString(), anyList());
    }

    @Test
    void findCategoriesByBook_whenCacheHitButEmpty_shouldThrowException() {

        String bookName = "BookWithNoCategories";
        String cacheKey = "categoriesByBook_" + bookName;
        when(categoryCache.containsKey(cacheKey)).thenReturn(true);
        when(categoryCache.get(cacheKey)).thenReturn(Collections.emptyList());


        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> categoryServiceImpl.findCategoriesByBook(bookName));
        assertEquals("Категории не найдены по книге: " + bookName, exception.getMessage());
        verify(categoryCache, times(1)).containsKey(cacheKey);
        verify(categoryCache, times(1)).get(cacheKey);
        verify(categoryRepository, never()).findCategoriesByBook(anyString());
    }
}