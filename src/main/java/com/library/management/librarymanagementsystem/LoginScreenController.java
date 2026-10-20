package com.library.management.librarymanagementsystem;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * LoginScreenController.java
 * 
 * Controller file for the login-screen.fxml. Provides functionality to the control elements within the GUI.
 * 
 * @author Dimitri Montgomery, Hayes Meekins, Preston Beachum, Tyler Gregory, Daniel Irwin
 * Date: 4/22/2025
 */
public class LoginScreenController{

    @FXML
    private Button loginButton;

    @FXML
    private Button createAccountButton;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField usernameField;

    @FXML
    private Label loginLabel;

    private static final String CSV_FILE_PATH = "src/main/resources/users.csv";

     /**
     * Sets up scene on application startup
     * @param url
     * @param resourceBundle
     */
    @FXML
    public void initialize() {
        try {
            loginLabel.setVisible(false);
            createCsvIfNotExists();
            setupEventHandlers();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * If there is no csv file at the designated to read data and write user data, a csv file will be created.
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
     * Sets up button events
     */
    @FXML
    private void setupEventHandlers() {
        loginButton.setOnAction(event -> {
            try {
                login();
            } 
            catch (IOException e) {
                e.printStackTrace();
                showAlert(AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
            }
        });
        createAccountButton.setOnAction(event -> {
            try {
                createUser();
            } 
            catch (IOException e) {
                e.printStackTrace();
                showAlert(AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
            }
        });
    }

    /**
     * Generates a list of user objects from csv file
     * @return list of user objects
     */
    private ArrayList<User> getUsers() {
        String file = CSV_FILE_PATH;
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
     * When login button is pressed this method will check if the username and password fields match and users stored in the csv file.
     * If there is a match it will check whether or not the user is an Admin. If so, the user will be directed to admin scene, otherwise
     * they will be directed to user scene.
     * @throws IOException
     */
    private void login() throws IOException {
        ArrayList<User> users = getUsers();
        for(int i = 0; i < users.size(); i++) {
            if(usernameField.getText().toLowerCase().equals(users.get(i).getUsername()) && passwordField.getText().equals(users.get(i).getPassword())) {
                User currentUser = users.get(i);
                if (currentUser.getAdmin()) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
                    Parent root = loader.load();
                    Scene adminScene = new Scene(root);
                    Stage stage = HelloApplication.getPrimaryStage();
                    stage.setScene(adminScene);
                    stage.setTitle("Library Management System");
                    stage.show();
                }
                else {
                    UserScreenController.setUserVariables(currentUser, users); //Passes user data to next scene
                    //Loads user-screen.fxml
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("user-screen.fxml"));
                    Parent root = loader.load();
                    // Set up the new scene
                    Scene userScene = new Scene(root);
                    Stage stage = HelloApplication.getPrimaryStage();
                    stage.setScene(userScene);
                    stage.setTitle("Library");
                    stage.show();
                }
            }
        }
        loginLabel.setVisible(true);
    }

    /**
     * Changes scene to sign-up-screen.fxml
     */
    private void createUser() throws IOException{
        SignUpScreenController.setAllUsers(getUsers());
        //Loads sign-up-screen.fxml
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sign-up-screen.fxml"));
        Parent root = loader.load();
        //Set up the new scene
        Scene signUp = new Scene(root);
        Stage stage = HelloApplication.getPrimaryStage();
        stage.setScene(signUp);
        stage.setTitle("Create Account");
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
