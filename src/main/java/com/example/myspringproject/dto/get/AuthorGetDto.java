package com.example.myspringproject.dto.get;

import com.example.myspringproject.model.Author;
import com.example.myspringproject.model.Book;
import java.util.List;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AuthorGetDto {
    private int id;
    private String authorName;
    private List<String> books;

    public AuthorGetDto(Author author) {
        this.id = author.getAuthorId();
        this.authorName = author.getAuthorName();
        if (author.getBooks() != null) {
            this.books = author.getBooks().stream()
                    .map(Book::getBookName)
                    .toList();
        }
    }
}