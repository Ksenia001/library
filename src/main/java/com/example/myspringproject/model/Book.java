package com.example.myspringproject.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue
    private int bookId;

    private String bookName;
    private String bookAuthor;

    //private int bookNumberOfPages;
    //private int bookYear;
}
