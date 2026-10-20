package com.library.management.librarymanagementsystem;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

/**
 * UserScreenController.java
 *
 * Controller for a patron's screen (user-screen.fxml): the books on the shelf, a search, checking
 * a book out, and the way to the return screen.
 *
 * @author Dimitri Montgomery, Hayes Meekins, Preston Beachum, Tyler Gregory, Daniel Irwin
 * Date: 4/22/2025, revised October 2026
 */
public class UserScreenController {
    @FXML
    private ListView<Book> bookListView;
    @FXML
    private Button checkoutButton;
    @FXML
    private Button logoutButton;
    @FXML
    private Button returnButton;
    @FXML
    private TextField searchTextField;

    @FXML
    public void initialize() {
        checkoutButton.setOnAction(event -> checkout());
        returnButton.setOnAction(event -> open("return-book.fxml", "Library"));
        logoutButton.setOnAction(event -> open("login-screen.fxml", "Library"));
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> loadBooks());
        loadBooks();
    }

    /** The books on the shelf that match the search. */
    private void loadBooks() {
        try {
            List<Book> onShelf = LibraryStore.get().books().stream().filter(b -> !b.isCheckedOut()).toList();
            bookListView.getItems().setAll(HelloViewController.matching(onShelf, searchTextField.getText()));
        } catch (IOException e) {
            Session.storageError(e);
        }
    }

    /**
     * Lends the selected book to the logged-in patron.
     */
    private void checkout() {
        Book selected = bookListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Session.alert(AlertType.WARNING, "No Selection", "Please select a book to check out.");
            return;
        }
        try {
            List<Book> books = LibraryStore.get().books();
            Optional<Book> book = books.stream().filter(selected::equals).findFirst();
            if (book.isEmpty() || book.get().isCheckedOut()) {
                Session.alert(AlertType.INFORMATION, "Not Available", "That book is no longer on the shelf.");
            } else {
                book.get().setBorrower(Session.user.getUsername());
                LibraryStore.get().saveBooks(books);
            }
            loadBooks();
        } catch (IOException e) {
            Session.storageError(e);
        }
    }

    private void open(String fxml, String title) {
        try {
            Session.show(fxml, title);
        } catch (IOException e) {
            Session.storageError(e);
        }
    }
}
