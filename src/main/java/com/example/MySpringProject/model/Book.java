package com.example.MySpringProject.model;

import lombok.*;

@Data
@Builder
public class Book {
    private String bookName;
    private String bookAuthor;
    private int bookISBN;
}
