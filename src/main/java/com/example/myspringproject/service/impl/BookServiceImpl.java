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
import com.example.myspringproject.service.BookService;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Primary
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;
    private final BookCache bookCache;
    private final CategoryCache categoryCache;

    @Override
    public List<Book> findAllBooks() {
        return bookRepository.findAllWithCategoriesAndAuthor();
    }

    @Override
    public Book findBookById(int id) {
        String cacheKey = "book_id_" + id;
        if (bookCache.containsKey(cacheKey)) {
            return bookCache.get(cacheKey).stream()
                    .filter(book -> book.getBookId() == id)
                    .findFirst()
                    .orElse(null);
        }
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Книга не найдена по id:" + id));
        if (book != null) {
            bookCache.put(cacheKey, List.of(book));
        }
        return book;
    }

    @Override
    public List<Book> searchBooks(String author, String title) {
        String cacheKey = "searchBooks_" + author + "_" + title;
        if (bookCache.containsKey(cacheKey)) {
            List<Book> cachedBooks = bookCache.get(cacheKey);
            if (cachedBooks.isEmpty()) {
                throw new EntityNotFoundException("Книга не найдены по автору: "
                        + author + " или названию: " + title);
            }
            return cachedBooks;
        }
        List<Book> books = bookRepository
                .findByAuthorAuthorNameContainingIgnoreCaseOrBookNameContainingIgnoreCase(
                author, title);
        if (books.isEmpty()) {
            throw new EntityNotFoundException("Книги не найдена по автору: "
                    + author + " или названию: " + title);
        }
        bookCache.put(cacheKey, books);
        return books;
    }

    @Override
    public List<Book> findBooksByCategory(String categoryName) {
        String cacheKey = "booksByCategory_" + categoryName;
        if (bookCache.containsKey(cacheKey)) {
            List<Book> cachedBooks = bookCache.get(cacheKey);
            if (cachedBooks.isEmpty()) {
                throw new EntityNotFoundException("Книги не найдены по категории: " + categoryName);
            }
            return cachedBooks;
        }
        List<Book> books = bookRepository.findByCategoryName(categoryName);
        if (books.isEmpty()) {
            throw new EntityNotFoundException("Книги не найдены по категории: " + categoryName);
        }
        bookCache.put(cacheKey, books);
        return books;
    }

    @Override
    public List<Book> findBooksByCategoryId(int categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new EntityNotFoundException("Категория не найдена с id: " + categoryId);
        }

        String cacheKey = "booksByCategoryId_" + categoryId;
        if (bookCache.containsKey(cacheKey)) {
            return bookCache.get(cacheKey);
        }
        List<Book> books = bookRepository.findByCategoryId(categoryId);
        bookCache.put(cacheKey, books);
        return books;
    }

    public List<Book> findBooksByAuthor(String authorName)  {
        String cacheKey = "booksByAuthor_" + authorName;
        if (bookCache.containsKey(cacheKey)) {
            List<Book> cachedBooks = bookCache.get(cacheKey);
            if (cachedBooks.isEmpty()) {
                throw new EntityNotFoundException("Книги не найдены по автору: "
                        + authorName);
            }
            return cachedBooks;
        }
        List<Book> books = bookRepository.findByAuthorName(authorName);
        if (books.isEmpty()) {
            throw new EntityNotFoundException("Книги не найдены по автору: " + authorName);
        }
        bookCache.put(cacheKey, books);
        return books;
    }

    @Override
    public List<Book> findBooksByAuthorId(int authorId) {
        if (!authorRepository.existsById(authorId)) {
            throw new EntityNotFoundException("Автор не найден с id: " + authorId);
        }

        String cacheKey = "booksByAuthorId_" + authorId;
        if (bookCache.containsKey(cacheKey)) {
            return bookCache.get(cacheKey);
        }
        List<Book> books = bookRepository.findByAuthorId(authorId);
        bookCache.put(cacheKey, books);
        return books;
    }

    @Override
    public Book createBook(BookCreateDto dto) {
        List<Book> existingBooks = bookRepository.findByAuthorId(dto.getAuthorId());
        boolean isDuplicate = existingBooks.stream()
                .anyMatch(book -> book.getBookName().equalsIgnoreCase(dto.getName()));

        if (isDuplicate) {
            throw new ValidationException(List.of("У автора уже есть книга с таким названием"));
        }

        Book book = new Book();
        book.setBookName(dto.getName());

        Author author = authorRepository.findById(dto.getAuthorId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Author not found with ID: " + dto.getAuthorId()));
        book.setAuthor(author);
        author.getBooks().add(book);

        if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(dto.getCategoryIds());
            if (categories.size() != dto.getCategoryIds().size()) {
                throw new IllegalArgumentException("Some categories not found");
            }

            categories.forEach(cat -> {
                if (cat.getBooks() == null) {
                    cat.setBooks(new ArrayList<>());
                }
                cat.getBooks().add(book);
            });
            book.setCategories(categories);
        }
        bookCache.clear();
        categoryCache.clear();
        return bookRepository.save(book);
    }

    @Override
    public Book updateBook(int id, BookUpdateDto dto) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book not found"));

        book.setBookName(dto.getBookName());

        if (dto.getAuthorId() != null) {
            Author author = authorRepository.findById(dto.getAuthorId())
                    .orElseThrow(() -> new EntityNotFoundException("Author not found"));
            book.setAuthor(author);
        }

        if (dto.getCategoriesIds() != null) {
            List<Category> categories = categoryRepository.findAllById(dto.getCategoriesIds());
            book.setCategories(categories);
        }
        bookCache.clear();
        categoryCache.clear();

        return bookRepository.save(book);
    }

    @Override
    public void deleteBookById(int id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book not found"));
        if (book.getAuthor() != null) {
            book.getAuthor().getBooks().remove(book);
        }
        bookRepository.deleteById(id);
        bookCache.clear();
        categoryCache.clear();
    }

    @Override
    public List<Book> createBooks(List<BookCreateDto> dtos) {
        validateDtos(dtos);

        Map<Integer, Author> authorsById = getAuthorsById(dtos);
        Map<String, Author> authorsByName = getAuthorsByName(dtos);

        Map<Integer, Category> categoriesById = getCategoriesById(dtos);
        Map<String, Category> categoriesByName = getCategoriesByName(dtos);

        List<Book> books =
                createBooksFromDtos(dtos, authorsById,
                        authorsByName, categoriesById, categoriesByName);

        bookRepository.saveAll(books);
        bookCache.clear();
        categoryCache.clear();
        return books;
    }

    private Map<String, Author> getAuthorsByName(List<BookCreateDto> dtos) {
        Set<String> authorNames = dtos.stream()
                .filter(dto -> dto.getAuthorName() != null)
                .map(BookCreateDto::getAuthorName)
                .collect(Collectors.toSet());
        List<Author> existingAuthorsByName = authorRepository.findByAuthorNameIn(authorNames);
        Map<String, Author> authorsByName = existingAuthorsByName.stream()
                .collect(Collectors.toMap(Author::getAuthorName, a -> a));

        List<Author> newAuthors = authorNames.stream()
                .filter(name -> !authorsByName.containsKey(name))
                .map(name -> {
                    Author author = new Author();
                    author.setAuthorName(name);
                    return author;
                })
                .toList();
        if (!newAuthors.isEmpty()) {
            authorRepository.saveAll(newAuthors);
            newAuthors.forEach(author -> authorsByName.put(author.getAuthorName(), author));
        }
        return authorsByName;
    }

    private Map<String, Category> getCategoriesByName(List<BookCreateDto> dtos) {
        Set<String> categoryNames = dtos.stream()
                .flatMap(dto -> dto.getCategoryNames() != null ? dto.getCategoryNames().stream()
                        : Stream.empty())
                .collect(Collectors.toSet());
        List<Category> existingCategoriesByName =
                categoryRepository.findByCategoryNameIn(categoryNames);
        Map<String, Category> categoriesByName = existingCategoriesByName.stream()
                .collect(Collectors.toMap(Category::getCategoryName, c -> c));

        List<Category> newCategories = categoryNames.stream()
                .filter(name -> !categoriesByName.containsKey(name))
                .map(name -> {
                    Category category = new Category();
                    category.setCategoryName(name);
                    return category;
                })
                .toList();
        if (!newCategories.isEmpty()) {
            categoryRepository.saveAll(newCategories);
            newCategories.forEach(
                    category -> categoriesByName.put(category.getCategoryName(), category));
        }
        return categoriesByName;
    }

    private void validateDtos(List<BookCreateDto> dtos) {
        for (BookCreateDto dto : dtos) {
            if ((dto.getAuthorId() != null && dto.getAuthorName() != null)
                    ||
                    (dto.getAuthorId() == null && dto.getAuthorName() == null)) {
                throw new ValidationException(List.of(
                        "Для каждой книги должен быть указан либо authorId, "
                                + "либо authorName, "
                                + "но не оба одновременно"));
            }
        }
    }

    private Map<Integer, Author> getAuthorsById(List<BookCreateDto> dtos) {
        Set<Integer> authorIds = dtos.stream()
                .filter(dto -> dto.getAuthorId() != null)
                .map(BookCreateDto::getAuthorId)
                .collect(Collectors.toSet());
        return authorRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(Author::getAuthorId, a -> a));
    }

    private Map<Integer, Category> getCategoriesById(List<BookCreateDto> dtos) {
        Set<Integer> categoryIds = dtos.stream()
                .flatMap(
                        dto -> dto.getCategoryIds() != null ? dto.getCategoryIds().stream()
                                : Stream.empty())
                .collect(Collectors.toSet());
        return categoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(Category::getCategoryId, c -> c));
    }

    private List<Book> createBooksFromDtos(
            List<BookCreateDto> dtos, Map<Integer, Author> authorsById,
            Map<String, Author> authorsByName, Map<Integer, Category> categoriesById,
            Map<String, Category> categoriesByName) {
        return dtos.stream()
                .map(dto -> {
                    Book book = new Book();
                    book.setBookName(dto.getName());

                    Author author = getAuthor(dto, authorsById, authorsByName);
                    book.setAuthor(author);
                    author.getBooks().add(book);

                    List<Category> categories = getCategories(
                            dto, categoriesById, categoriesByName);
                    book.setCategories(categories);
                    categories.forEach(cat -> {
                        if (cat.getBooks() == null) {
                            cat.setBooks(new ArrayList<>());
                        }
                        cat.getBooks().add(book);
                    });

                    return book;
                })
                .toList();
    }

    private Author getAuthor(BookCreateDto dto,
                             Map<Integer, Author> authorsById, Map<String, Author> authorsByName) {
        if (dto.getAuthorId() != null) {
            Author author = authorsById.get(dto.getAuthorId());
            if (author == null) {
                throw new EntityNotFoundException("Автор не найден с ID: " + dto.getAuthorId());
            }
            return author;
        } else {
            return authorsByName.get(dto.getAuthorName());
        }
    }

    private List<Category> getCategories(BookCreateDto dto,
                                         Map<Integer, Category> categoriesById,
                                         Map<String, Category> categoriesByName) {
        List<Category> categories = new ArrayList<>();
        if (dto.getCategoryIds() != null) {
            for (Integer categoryId : dto.getCategoryIds()) {
                Category category = categoriesById.get(categoryId);
                if (category == null) {
                    throw new EntityNotFoundException("Категория не найдена с ID: " + categoryId);
                }
                categories.add(category);
            }
        }
        if (dto.getCategoryNames() != null) {
            for (String categoryName : dto.getCategoryNames()) {
                Category category = categoriesByName.get(categoryName);
                categories.add(category);
            }
        }
        return categories;
    }
}

