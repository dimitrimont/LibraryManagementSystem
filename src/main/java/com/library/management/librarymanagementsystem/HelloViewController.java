package com.library.management.librarymanagementsystem;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * HelloViewController.java
 *
 * Controller for the administrator's screen (hello-view.fxml): the whole catalogue, including the
 * books that are checked out and who has them; adding, editing and deleting books; and making
 * another account an administrator.
 *
 * @author Dimitri Montgomery, Hayes Meekins, Preston Beachum, Tyler Gregory, Daniel Irwin
 * Date: 4/22/2025, revised October 2026
 */
public class HelloViewController {
    @FXML
    private TextField searchTextField;
    @FXML
    private TextField authorNameTextField;
    @FXML
    private TextField bookNameTextField;
    @FXML
    private TextField isbnTextField;
    @FXML
    private TextField categoryTextField;
    @FXML
    private TextField promoteTextField;
    @FXML
    private Button submitButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button logoutButton;
    @FXML
    private Button promoteButton;
    @FXML
    private ListView<Book> bookListView;

    @FXML
    public void initialize() {
        submitButton.setOnAction(event -> addBook());
        deleteButton.setOnAction(event -> deleteBook());
        logoutButton.setOnAction(event -> logout());
        promoteButton.setOnAction(event -> promote());
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> loadBooks());
        loadBooks();
    }

    /**
     * Shows the catalogue, or the books matching the search once it is three characters long.
     */
    private void loadBooks() {
        try {
            bookListView.getItems().setAll(matching(LibraryStore.get().books(), searchTextField.getText()));
        } catch (IOException e) {
            Session.storageError(e);
        }
    }

    /** The books whose author, title, ID or genre contains the search, ignoring case. */
    static List<Book> matching(List<Book> books, String search) {
        String s = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        if (s.length() < 3) return books;
        return books.stream()
                .filter(b -> (b.getAuthor() + "\n" + b.getTitle() + "\n" + b.getBook_id() + "\n" + b.getGenre())
                        .toLowerCase(Locale.ROOT).contains(s))
                .toList();
    }

    private void addBook() {
        String author = authorNameTextField.getText().trim();
        String title = bookNameTextField.getText().trim();
        String isbn = isbnTextField.getText().trim();
        String genre = categoryTextField.getText().trim();
        if (author.isEmpty() || title.isEmpty() || isbn.isEmpty() || genre.isEmpty()) {
            Session.alert(AlertType.WARNING, "Incomplete Entry", "Please fill in all fields.");
            return;
        }
        try {
            List<Book> books = LibraryStore.get().books();
            Optional<Book> same = books.stream().filter(b -> b.getBook_id().equals(isbn)).findFirst();
            if (same.isPresent()) {
                Session.alert(AlertType.ERROR, "Duplicate ISBN", "A book with this ISBN already exists: " + same.get()
                        + (same.get().isCheckedOut() ? ". It is checked out, not gone." : "."));
                return;
            }
            books.add(new Book(author, title, isbn, genre));
            LibraryStore.get().saveBooks(books);
            authorNameTextField.clear();
            bookNameTextField.clear();
            isbnTextField.clear();
            categoryTextField.clear();
            loadBooks();
        } catch (IOException e) {
            Session.storageError(e);
        }
    }

    private void deleteBook() {
        Book selected = bookListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Session.alert(AlertType.WARNING, "No Selection", "Please select a book to delete.");
            return;
        }
        try {
            List<Book> books = LibraryStore.get().books();
            Optional<Book> book = books.stream().filter(selected::equals).findFirst();
            if (book.isPresent() && book.get().isCheckedOut()) {
                Session.alert(AlertType.WARNING, "Checked Out",
                        book.get().getBorrower() + " has this book. It can be deleted once it is returned.");
                return;
            }
            books.remove(selected);
            LibraryStore.get().saveBooks(books);
            loadBooks();
        } catch (IOException e) {
            Session.storageError(e);
        }
    }

    /**
     * Opens the edit screen for the selected book.
     */
    @FXML
    public void handleSwitchScene(ActionEvent event) {
        Book selected = bookListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Session.alert(AlertType.WARNING, "No Selection", "Please select a book to edit.");
            return;
        }
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("edit.fxml"));
            Parent editRoot = fxmlLoader.load();
            editController controller = fxmlLoader.getController();
            controller.setBookData(selected);
            Stage stage = HelloApplication.getPrimaryStage();
            stage.setScene(new Scene(editRoot));
            stage.setTitle("Edit Book");
            stage.show();
        } catch (IOException e) {
            Session.storageError(e);
        }
    }

    /**
     * Makes the named account an administrator.
     */
    private void promote() {
        String name = promoteTextField.getText().trim().toLowerCase();
        try {
            List<User> users = LibraryStore.get().users();
            Optional<User> user = users.stream().filter(u -> u.getUsername().equals(name)).findFirst();
            if (user.isEmpty()) {
                Session.alert(AlertType.WARNING, "No Such Account", "There is no account called \"" + name + "\".");
            } else if (user.get().getAdmin()) {
                Session.alert(AlertType.INFORMATION, "Already an Administrator", name + " is already an administrator.");
            } else {
                user.get().setAdmin(true);
                LibraryStore.get().saveUsers(users);
                promoteTextField.clear();
                Session.alert(AlertType.INFORMATION, "Administrator", name + " is now an administrator.");
            }
        } catch (IOException e) {
            Session.storageError(e);
        }
    }

    private void logout() {
        try {
            Session.show("login-screen.fxml", "Library");
        } catch (IOException e) {
            Session.storageError(e);
        }
    }
}
