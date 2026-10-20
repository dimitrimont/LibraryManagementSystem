package com.library.management.librarymanagementsystem;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
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
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * UserScreenController.java
 * 
 * Controller file for the user-screen.fxml. Provides functionality to the control elements within the GUI.
 * 
 * @author Dimitri Montgomery, Hayes Meekins, Preston Beachum, Tyler Gregory, Daniel Irwin
 * Date: 4/22/2025
 */
public class UserScreenController {

    @FXML
    private ListView<String> bookListView;

    @FXML
    private Button checkoutButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Button returnButton;

    @FXML
    private TextField searchTextField;

    private static User currentUser;

    private static ArrayList<User> allUsers;

    private static final String CSV_FILE_PATH_BOOKS = "src/main/resources/inventory.csv";
    private static final String CSV_FILE_PATH_USERS = "src/main/resources/users.csv";

    /**
     * Sets up scene on application startup
     */
    @FXML
    public void initialize() {
        try {
            // Create CSV file if it doesn't exist
            createCsvIfNotExists();

            // Load books from CSV file
            loadBooks();

            // Set up event handlers
            setupEventHandlers();
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
        }
    }

    /**
     * Creates a CSV file to store book data if one does not already exist
     * @throws IOException
     */
    private void createCsvIfNotExists() throws IOException {
        File csvFile = new File(CSV_FILE_PATH_BOOKS);
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
     * Sets current user and allUser variables
     * @param currentUser current user accessing library
     * @param allUsers all users in library.
     */
    static void setUserVariables(User currentUser, ArrayList<User> allUsers) {
        UserScreenController.currentUser = currentUser;
        UserScreenController.allUsers = allUsers;
    }
    /**
     * Handles each event that may occur when user interacts with the app
     */
    @FXML
    private void setupEventHandlers() {
        // Submit button for adding new books
        checkoutButton.setOnAction(event -> {
            try {
                checkout();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Edit button for switching to edit scene
        returnButton.setOnAction(event -> {
            loadReturnScene();
        });

        // Delete button for removing selected books
        logoutButton.setOnAction(event -> {
            logout();
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

    /*
     * Writes all updated User Information to CSV file
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
     * Retrieves selected book from user, adds book to users inventory, removes book from library inventory
     * and writes new data to csv.
     */
    private void checkout() {
        try {
            String selectedBook = bookListView.getSelectionModel().getSelectedItem();
            if (selectedBook!= null) {
                UserScreenController.currentUser.getBooks().add(Book.fromCsvString(selectedBook));
                deleteBook();
                writeAllUsers();
            }
        }
        catch(IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Checkout Error " + e.getMessage());
        }
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
            writeBooksToCsv(updatedBooks);

            // Reload the book list
            loadBooks();
        
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
        return Files.readAllLines(Paths.get(CSV_FILE_PATH_BOOKS));
    }

    /**
     * Writes list of books to a csv file
     * @param books list of all books in libraries inventory
     * @throws IOException
     */
    private void writeBooksToCsv(List<String> books) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE_PATH_BOOKS))) {
            for (String book : books) {
                writer.write(book);
                writer.newLine();
            }
        }
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
     * Generates a list of user objects from csv file
     * @return list of user objects
     */
    public ArrayList<User> getUsers() {
        String file = CSV_FILE_PATH_USERS;
        BufferedReader reader;
        String line;
        ArrayList<User> users = new ArrayList<>();
        try {
            reader = new BufferedReader(new FileReader(file));
            while((line = reader.readLine()) != null) {
                users.add(User.fromCSVString(line));
            }
            return users;
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Invalid User Data", "There was a problem reading user data");
        }
        return null;
    }

    /**
     * Loads up return book scene and sets its
     */
    private void loadReturnScene() {
        try {
           //Sets controllers instance variables
           ReturnBookController.setUserVariables(UserScreenController.currentUser, UserScreenController.allUsers);

           //Loads return-book.fxml
           FXMLLoader loader = new FXMLLoader(getClass().getResource("return-book.fxml"));
           Parent root = loader.load();

           // Set up the new scene
           Scene returnScene = new Scene(root);
           Stage stage = HelloApplication.getPrimaryStage();
           stage.setScene(returnScene);
           stage.setTitle("Library");
           stage.show();
        }
        catch(IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Navigation Error ", e.getMessage());
        }
    }

    /**
     * Sets user info to null and changes scene to login
     */
    private void logout() {
        try {
            UserScreenController.currentUser = null;
            UserScreenController.allUsers = null;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login-screen.fxml"));
            Parent root = loader.load();

           // Set up the new scene
           Scene userScene = new Scene(root);
           Stage stage = HelloApplication.getPrimaryStage();
           stage.setScene(userScene);
           stage.setTitle("Library");
           stage.show();
        }
        catch(IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Navigation Error ", e.getMessage());
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


