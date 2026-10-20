package com.library.management.librarymanagementsystem;

import java.io.IOException;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

/**
 * ReturnBookController.java
 *
 * Controller for return-book.fxml: the books the logged-in patron has, and returning one to the shelf.
 *
 * @author Dimitri Montgomery, Hayes Meekins, Preston Beachum, Tyler Gregory, Daniel Irwin
 * Date: 4/22/2025, revised October 2026
 */
public class ReturnBookController {
    @FXML
    private ListView<Book> bookListView;
    @FXML
    private Button cancelButton;
    @FXML
    private Button submitButton;

    @FXML
    public void initialize() {
        submitButton.setOnAction(event -> returnBook());
        cancelButton.setOnAction(event -> returnToUserScene());
        try {
            String me = Session.user.getUsername();
            bookListView.getItems().setAll(LibraryStore.get().books().stream()
                    .filter(b -> b.getBorrower().equals(me)).toList());
        } catch (IOException e) {
            Session.storageError(e);
        }
    }

    /**
     * Puts the selected book back on the shelf.
     */
    private void returnBook() {
        Book selected = bookListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Session.alert(AlertType.WARNING, "No Selection", "Please select a book to return.");
            return;
        }
        try {
            List<Book> books = LibraryStore.get().books();
            books.stream().filter(selected::equals).findFirst().ifPresent(b -> b.setBorrower(""));
            LibraryStore.get().saveBooks(books);
            returnToUserScene();
        } catch (IOException e) {
            Session.storageError(e);
        }
    }

    private void returnToUserScene() {
        try {
            Session.show("user-screen.fxml", "Library");
        } catch (IOException e) {
            Session.storageError(e);
        }
    }
}
