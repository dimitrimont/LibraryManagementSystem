package com.library.management.librarymanagementsystem;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * SignUpScreenController.java
 * 
 * Controller file for the sign-up-screen.fxml. Provides functionality to the control elements within the GUI.
 * 
 * @author Dimitri Montgomery, Hayes Meekins, Preston Beachum, Tyler Gregory, Daniel Irwin
 * Date: 4/22/2025
 */
public class SignUpScreenController {

    @FXML
    private Button cancelButton;

    @FXML
    private Button createAccountButton;

    @FXML
    private CheckBox isAdmin;

    @FXML
    private TextField passwordTextField;

    @FXML
    private TextField usernameTextField;

    private static ArrayList<User> allUsers;

    private static final String CSV_FILE_PATH = "src/main/resources/users.csv";

    /**
     * Calls all methods needed to initialize scene
     */
    @FXML
    public void initialize() {
        try {
            // Set up event handlers
            setupEventHandlers();
        } 
        catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
        }
    }

    /**
     * Defines how to handle all possible events that could occur in the scene
     */
    private void setupEventHandlers() throws IOException{
        createAccountButton.setOnAction(event -> {
            try {
                createAccount();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(AlertType.ERROR, "Error", "Account creation error " + e.getMessage());
            }
        });

        cancelButton.setOnAction(event -> {
            try {
                switchToLogin();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(AlertType.ERROR, "Error", "Navigation Error " + e.getMessage());
            }
        });
    }

    /**
     * Adds a new user to ArrayList of libraries users based on information entered into text fields. Writes data to csv and
     * switches scene back to login
     */
    private void createAccount() throws IOException{
        User userToAdd = new User(usernameTextField.getText().toLowerCase(), passwordTextField.getText(), isAdmin.isSelected());
        if (!checkForMatch(userToAdd)) {
            allUsers.add(userToAdd);
            writeAllUsers();
            switchToLogin();
        }
        else {
            showAlert(AlertType.INFORMATION, "Account Creation Failed", "Username is taken. Please try again");
        }
    }

    /**
     * Checks if username exists already. Returns true if match is found, otherwise false
     * @param userToAdd User object to be created
     * @return
     */
    private boolean checkForMatch(User userToAdd) {
        for(User user : allUsers) {
            if (user.getUsername().equals(userToAdd.getUsername())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Changes scene back to login
     */
    private void switchToLogin() throws IOException {
        //Loads login-screen.fxml
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login-screen.fxml"));
        Parent root = loader.load();
        // Set up the new scene
        Scene loginScene = new Scene(root);
        Stage stage = HelloApplication.getPrimaryStage();
        stage.setScene(loginScene);
        stage.setTitle("Library");
        stage.show();
    }

    /**
     * Writes user data to csv file
     */
    private void writeAllUsers() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE_PATH))) {
            for (int i = 0; i < allUsers.size(); i++) {
                writer.write(allUsers.get(i).toCSVString());
                writer.newLine();
            }
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

    /**
     * Sets controllers allUsers instance variable
     */
    public static void setAllUsers(ArrayList<User> allUsers) {
        SignUpScreenController.allUsers = allUsers;
    }
}
