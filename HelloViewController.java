package com.library.management.librarymanagementsystem;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * HelloViewController.java
 * 
 * Controller file for the hello-view.fxml. Provides functionality to the control elements within the GUI.
 * 
 * @author Dimitri Montgomery, Hayes Meekins, Preston Beachum, Tyler Gregory, Daniel Irwin
 * Date: 4/22/2025
 */
public class HelloViewController implements Initializable {
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
    private Button submitButton;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button logoutButton;

    @FXML
    private ListView<String> bookListView;

    private static final String CSV_FILE_PATH = "src/main/resources/inventory.csv";

    /**
     * Sets up scene on application startup
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Create CSV file if it doesn't exist
            createCsvIfNotExists();
            // Load books from CSV file
            loadBooks();
            // Set up event handlers
            setupEventHandlers();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a CSV file to store book data if one does not already exist
     * @throws IOException
     */
    private void createCsvIfNotExists() throws IOException {
        File csvFile = new File(CSV_FILE_PATH);
        if (!csvFile.exists()) {
            // Create directory if it doesn't exist
            File parentDir = csvFile.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }

            // Create empty CSV file
            csvFile.createNewFile();
        }
    }

    /**
     * Handles each event that may occur when user interacts with the app
     */
    private void setupEventHandlers() {
        // Submit button for adding new books
        submitButton.setOnAction(event -> {
            try {
                addBook();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Edit button for switching to edit scene
        editButton.setOnAction(event -> {
            try {
                String selectedBook = bookListView.getSelectionModel().getSelectedItem();
                if (selectedBook == null) {
                    showAlert(AlertType.WARNING, "No Selection", "Please select a book to edit.");
                    return;
                }

                // Get the selected book data first
                editBook();
                // Then switch to the edit scene
                handleSwitchScene(event);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
            }
        });

        // Delete button for removing selected books
        deleteButton.setOnAction(event -> {
            try {
                deleteBook();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        logoutButton.setOnAction(event -> {
            try {
                logout();
            } 
            catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Search text field functionality
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                searchBook(newValue);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Shows all books in the libraries inventory system.
     * @throws IOException
     */
    private void loadBooks() throws IOException {
        bookListView.getItems().clear();

        List<String> books = readAllBooksFromCsv();
        bookListView.getItems().addAll(books);
    }

    /**
     * Reads all lines from csv and returns them.
     * @return a list of items from csv file or a blank ArrayList if an exception is found
     * @throws IOException
     */
    private List<String> readAllBooksFromCsv() throws IOException {
        try {
            return Files.readAllLines(Paths.get(CSV_FILE_PATH));
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    /**
     * Writes list of books to a csv file
     * @param books list of all books in libraries inventory
     * @throws IOException
     */
    private void writeBooksToCsv(List<String> books) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE_PATH))) {
            for (String book : books) {
                writer.write(book);
                writer.newLine();
            }
        }
    }

    /**
     * Creates a new book object and uses fields entered by to identify the books parameters. When
     * the book has been created with user information and the book is unique it is added to the librarys
     * inventory.
     * @throws IOException
     */
    private void addBook() throws IOException {
        String author = authorNameTextField.getText().trim();
        String title = bookNameTextField.getText().trim();
        String isbn = isbnTextField.getText().trim();
        String genre = categoryTextField.getText().trim();

        if (author.isEmpty() || title.isEmpty() || isbn.isEmpty() || genre.isEmpty()) {
            // Show alert if fields are empty
            showAlert(AlertType.WARNING, "Incomplete Entry", "Please fill in all fields.");
            return;
        }

        Book newBook = new Book(author, title, isbn, genre);

        // Check if ISBN already exists
        List<String> allBooks = readAllBooksFromCsv();
        boolean isbnExists = allBooks.stream()
                .map(Book::fromCsvString)
                .anyMatch(b -> b != null && b.getBook_id().equals(isbn));

        if (!isbnExists) {
            // Add the new book
            allBooks.add(newBook.toCsvString());
            writeBooksToCsv(allBooks);

            // Clear input fields
            authorNameTextField.clear();
            bookNameTextField.clear();
            isbnTextField.clear();
            categoryTextField.clear();

            // Reload the book list
            loadBooks();
        } else {
            // Show alert if ISBN exists
            showAlert(AlertType.ERROR, "Duplicate ISBN", "A book with this ISBN already exists.");
        }
    }
    
    /**
     * Checks if user has a book selected. If the selection is valid, the program will allow the user to
     * make adjustments to one or more of the books fields or cancel if they decide not to make any changes.
     * If the selection is invalid then a message will be displayed to explain to the user why the edit page
     * did not appear.
     * @throws IOException
     */
    private void editBook() throws IOException {
        String selectedBook = bookListView.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showAlert(AlertType.WARNING, "No Selection", "Please select a book to edit.");
            return;
        }

        Book bookToEdit = Book.fromCsvString(selectedBook);
        if (bookToEdit == null) {
            showAlert(AlertType.ERROR, "Invalid Book", "The selected book data is invalid.");
            return;
        }

        // Set edit form fields
        authorNameTextField.setText(bookToEdit.getAuthor());
        bookNameTextField.setText(bookToEdit.getTitle());
        isbnTextField.setText(bookToEdit.getBook_id());
        categoryTextField.setText(bookToEdit.getGenre());
    }

    /**
     * Method will check if the user has made a selection. If the selection is valid the method will create a new list
     * of all books that do not match the users selection and overwrite the csv with the new list of books. If the selection
     * is invalid a message will be displayed to explain to the user why nothing happened when the delete button was pressed.
     * @throws IOException
     */
    private void deleteBook() throws IOException {
        String selectedBook = bookListView.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showAlert(AlertType.WARNING, "No Selection", "Please select a book to delete.");
            return;
        }

        Book bookToDelete = Book.fromCsvString(selectedBook);
        if (bookToDelete == null) {
            showAlert(AlertType.ERROR, "Invalid Book", "The selected book data is invalid.");
            return;
        }

        String isbnToDelete = bookToDelete.getBook_id();

        // Read all books from CSV
        List<String> allBooks = readAllBooksFromCsv();

        // Filter out the book to delete
        List<String> updatedBooks = new ArrayList<>();
        for (String book : allBooks) {
            Book b = Book.fromCsvString(book);
            if (b == null || !b.getBook_id().equals(isbnToDelete)) {
                updatedBooks.add(book);
            }
        }

        // Write updated list back to CSV
        writeBooksToCsv(updatedBooks);

        // Reload the book list
        loadBooks();
    }

    /**
     * Each time a user enters something in the search bar the method will check if any of the books
     * contain the search text. If a book matches the search text then it will be added to the list
     * of matching books. That list will then be viewable to the user.
     * @param searchText text entered into search bar
     * @throws IOException
     */
    private void searchBook(String searchText) throws IOException {
        if (searchText == null || searchText.trim().length() < 3) {
            loadBooks(); // Show all books if search term is too short
            return;
        }

        searchText = searchText.toLowerCase().trim();

        // Load all books first
        List<String> allBooks = readAllBooksFromCsv();
        List<String> matchingBooks = new ArrayList<>();

        // Filter books based on search text
        for (String bookLine : allBooks) {
            if (bookLine.toLowerCase().contains(searchText)) {
                matchingBooks.add(bookLine);
            }
        }

        // Update the ListView
        bookListView.getItems().clear();
        bookListView.getItems().addAll(matchingBooks);
    }

    /**
     * Switches the window to the book edit screen when the edit button is clicked if a selection has been made.
     * If there is no selection or the selection is invalid a message will appear explaining why nothing happened
     * when the button was pressed.
     * @param event alerts the program that the edit button was pressed
     */
    @FXML
    public void handleSwitchScene(ActionEvent event) {
        try {
            String selectedBook = bookListView.getSelectionModel().getSelectedItem();
            if (selectedBook == null) {
                showAlert(AlertType.WARNING, "No Selection", "Please select a book to edit.");
                return;
            }

            Book bookToEdit = Book.fromCsvString(selectedBook);
            if (bookToEdit == null) {
                showAlert(AlertType.ERROR, "Invalid Book", "The selected book data is invalid.");
                return;
            }

            // Load the edit view FXML
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("edit.fxml"));
            Parent editRoot = fxmlLoader.load();

            // Get the controller and set the book data
            editController editController = fxmlLoader.getController();
            editController.setBookData(bookToEdit);

            // Set up the new scene
            Scene editScene = new Scene(editRoot);
            Stage stage = HelloApplication.getPrimaryStage();
            stage.setScene(editScene);
            stage.setTitle("Edit Book");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Navigation Error",
                    "Could not load the edit view: " + e.getMessage());
        }
    }

    /**
     * Handles switch scene back to login-screen
     * @throws IOException
     */
    private void logout() throws IOException {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login-screen.fxml"));
            Parent root = loader.load();

           // Set up the new scene
           Scene userScene = new Scene(root);
           Stage stage = HelloApplication.getPrimaryStage();
           stage.setScene(userScene);
           stage.setTitle("Library");
           stage.show();
    }


    /**
     * Method displays an alert
     * @param alertType defines what type of alert the user will see
     * @param title title of the alert
     * @param message message displayed to user
     */
    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}