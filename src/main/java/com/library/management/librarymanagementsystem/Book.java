package com.library.management.librarymanagementsystem;

import java.util.List;
import java.util.Objects;

/**
 * Book.java
 *
 * Defines a Book object to be used for storing book information in Library Management System.
 * A book that is checked out stays in the catalogue with the borrower's username, so its ISBN
 * cannot be given to another book while it is away, and the admin can see who has it.
 *
 * @author Dimitri Montgomery, Hayes Meekins, Preston Beachum, Tyler Gregory, Daniel Irwin
 * Date: 4/22/2025, revised October 2026
 */
public class Book {
    static final List<String> HEADER = List.of("author", "title", "isbn", "genre", "borrower");

    private String author;
    private String title;
    private String book_id; // ISBN or library ID, unique in the catalogue
    private String genre;
    private String borrower; // username of the patron who has it, or "" when it is on the shelf

    /**
     * Constructor for a book on the shelf
     * @param author author of the book
     * @param title title of the book
     * @param book_id unique book id
     * @param genre genre of the book
     */
    public Book(String author, String title, String book_id, String genre) {
        this(author, title, book_id, genre, "");
    }

    public Book(String author, String title, String book_id, String genre, String borrower) {
        this.author = author;
        this.title = title;
        this.book_id = book_id;
        this.genre = genre;
        this.borrower = borrower == null ? "" : borrower;
    }

    // Getter and setter methods
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBook_id() { return book_id; }
    public void setBook_id(String book_id) { this.book_id = book_id; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getBorrower() { return borrower; }
    public void setBorrower(String borrower) { this.borrower = borrower == null ? "" : borrower; }
    public boolean isCheckedOut() { return !borrower.isEmpty(); }

    /** The book's fields, in the order of {@link #HEADER}. */
    List<String> toRow() {
        return List.of(author, title, book_id, genre, borrower);
    }

    /** A book from one row of books.csv, or null if the row does not have five fields. */
    static Book fromRow(List<String> row) {
        if (row.size() != HEADER.size()) return null;
        return new Book(row.get(0), row.get(1), row.get(2), row.get(3), row.get(4));
    }

    /** How the book reads in a list. */
    @Override
    public String toString() {
        String s = author + ", " + title + ", ISBN " + book_id + ", " + genre;
        return isCheckedOut() ? s + " (checked out by " + borrower + ")" : s;
    }

    /**
     * Two books are the same book if they have the same ID.
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof Book b && Objects.equals(book_id, b.book_id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(book_id);
    }
}
