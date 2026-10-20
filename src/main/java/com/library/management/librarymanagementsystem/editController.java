package com.library.management.librarymanagementsystem;

import java.io.IOException;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;

/**
 * editController.java
 *
 * Controller for edit.fxml: changes one book's author, title, ID or genre. A checked-out book keeps
 * its borrower.
 *
 * @author Dimitri Montgomery, Hayes Meekins, Preston Beachum, Tyler Gregory, Daniel Irwin
 * Date: 4/22/2025, revised October 2026
 */
public class editController {
    @FXML
    private TextField editAuthorTextField;
    @FXML
    private TextField editTitleTextField;
    @FXML
    private TextField editISBNTextField;
    @FXML
    private TextField editCategoryTextField;

    private String originalISBN; // which book is being edited

    /** Called by HelloViewController before the scene is shown. */
    public void setBookData(Book book) {
        editAuthorTextField.setText(book.getAuthor());
        editTitleTextField.setText(book.getTitle());
        editISBNTextField.setText(book.getBook_id());
        editCategoryTextField.setText(book.getGenre());
        originalISBN = book.getBook_id();
    }

    @FXML
    public void submitEdit(ActionEvent event) {
        String author = editAuthorTextField.getText().trim();
        String title = editTitleTextField.getText().trim();
        String isbn = editISBNTextField.getText().trim();
        String genre = editCategoryTextField.getText().trim();
        if (author.isEmpty() || title.isEmpty() || isbn.isEmpty() || genre.isEmpty()) {
            Session.alert(AlertType.WARNING, "Incomplete Entry", "Please fill in all fields.");
            return;
        }
        try {
            List<Book> books = LibraryStore.get().books();
            if (!isbn.equals(originalISBN) && books.stream().anyMatch(b -> b.getBook_id().equals(isbn))) {
                Session.alert(AlertType.ERROR, "Duplicate ISBN", "A different book with this ISBN already exists.");
                return;
            }
            Book book = books.stream().filter(b -> b.getBook_id().equals(originalISBN)).findFirst().orElse(null);
            if (book == null) {
                Session.alert(AlertType.ERROR, "Book Not Found", "The book you're trying to edit could not be found.");
                return;
            }
            book.setAuthor(author);
            book.setTitle(title);
            book.setBook_id(isbn);
            book.setGenre(genre);
            LibraryStore.get().saveBooks(books);
            Session.show("hello-view.fxml", "Library Management System");
        } catch (IOException e) {
            Session.storageError(e);
        }
    }

    @FXML
    public void cancelEdit(ActionEvent event) {
        try {
            Session.show("hello-view.fxml", "Library Management System");
        } catch (IOException e) {
            Session.storageError(e);
        }
    }
}
