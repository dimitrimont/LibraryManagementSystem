package com.library.management.librarymanagementsystem;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

/**
 * UserScreenController.java
 * 
 * Controller file for the return-book.fxml. Provides functionality to the control elements within the GUI.
 * 
 * @author Dimitri Montgomery, Hayes Meekins, Preston Beachum, Tyler Gregory, Daniel Irwin
 * Date: 4/22/2025
 */
public class ReturnBookController {

    @FXML
    private ListView<String> bookListView;

    @FXML
    private Button cancelButton;

    @FXML
    private Button submitButton;

    private static User currentUser;

    private static ArrayList<User> allUsers;

    private static final String CSV_FILE_PATH_BOOKS = "src/main/resources/inventory.csv";
    private static final String CSV_FILE_PATH_USERS = "src/main/resources/users.csv";

    /**
     * Sets up scene on application startup
     * @param url
     * @param resourceBundle
     */
    @FXML
    public void initialize() {
        try {
            // Set up event handlers
            setupEventHandlers();
            
            //Load books for listView
            loadBooks();
        } 
        catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Could not add book to inventory" + e.getMessage());
        }
    }

    /**
     * Sets controllers currentUser and allUsers instance variables
     * @param currentUser user currently accessing library
     * @param allUsers all users in library
     */
    public static void setUserVariables(User currentUser, ArrayList<User> allUsers) {
        ReturnBookController.currentUser = currentUser;
        ReturnBookController.allUsers = allUsers;
    }

    /**
     * Defines how to handle all events in scene
     */
    private void setupEventHandlers() {
        // Submit button for adding new books
        submitButton.setOnAction(event -> {
            try {
                returnBook();
                returnToUserScene();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Edit button for switching to edit scene
        cancelButton.setOnAction(event -> {
            try {
                returnToUserScene();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Retrieves selected book from user, removes it from users inventory, adds it to library inventory
     * and writes new data to csv file.
     */
    private void returnBook() {
        String selectedBook = bookListView.getSelectionModel().getSelectedItem();
        currentUser.getBooks().remove(Book.fromCsvString(selectedBook));
        addBook();
        writeAllUsers();
    }
    
    /**
     * Retrieves all books in libraries inventory from csv
     * @return a list of all books in librarys inventory
     * @throws IOException
     */
    private List<String> readAllBooksFromCsv() throws IOException {
        try {
            return Files.readAllLines(Paths.get(CSV_FILE_PATH_BOOKS));
        } 
        catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Adds selected book to libraries inventory and writes new data to csv
     */
    private void addBook() {
        try {
            List<String> allBooks = readAllBooksFromCsv();
            String selectedBook = bookListView.getSelectionModel().getSelectedItem();
            allBooks.add(selectedBook);
            writeBooksToCsv(allBooks);

        }
        catch(IOException e) {
            showAlert(AlertType.ERROR, "Error", "Could not add book to inventory" + e.getMessage());
        }

    }

    /**
     * Writes list of books to a csv file
     * @param books list of all books in libraries inventory
     * @throws IOException
     */
    private void writeBooksToCsv(List<String> books) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE_PATH_BOOKS))) {
            for (String book : books) {
                book = book.trim();
                writer.write(book);
                writer.newLine();
            }
        }
    }

    /**
     * Writes all users to libraries inventory
     */
    private void writeAllUsers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE_PATH_USERS))) {
            for (int i = 0; i < allUsers.size(); i++) {
                writer.write(allUsers.get(i).toCSVString());
                writer.newLine();
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
        }
    }

    /**
     * Loads list of books in inventory to bookListView so that it is visible
     * to user
     * @throws IOException
     */
    private void loadBooks() throws IOException {
        bookListView.getItems().clear();
        List<String> books = new ArrayList<>();
        for(int i = 0; i < currentUser.getBooks().size(); i++) {
            books.add(currentUser.getBooks().get(i).toCsvString());
        }
        bookListView.getItems().addAll(books);
    }

    /**
     * Handles switch scene back to main screen.
     * @param event
     */
    private void returnToUserScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("user-screen.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = HelloApplication.getPrimaryStage();
            stage.setScene(scene);
            stage.setTitle("Library Management System");
            stage.show();
        }
        catch (IOException e){
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Navigation Error", "Could not navigate back to main view: " + e.getMessage());
        }
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
