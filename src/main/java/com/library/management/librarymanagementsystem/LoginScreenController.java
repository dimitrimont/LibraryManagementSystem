package com.library.management.librarymanagementsystem;

import java.io.IOException;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * LoginScreenController.java
 *
 * Controller file for the login-screen.fxml. Provides functionality to the control elements within the GUI.
 *
 * @author Dimitri Montgomery, Hayes Meekins, Preston Beachum, Tyler Gregory, Daniel Irwin
 * Date: 4/22/2025, revised October 2026
 */
public class LoginScreenController {

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

    /**
     * Sets up scene on application startup
     */
    @FXML
    public void initialize() {
        Session.user = null;
        loginLabel.setVisible(false);
        loginButton.setOnAction(event -> login());
        createAccountButton.setOnAction(event -> open("sign-up-screen.fxml", "Create Account"));
    }

    /**
     * Checks the username and password against the stored accounts. An admin goes to the
     * catalogue screen, a patron to the borrowing screen; anyone else sees "Invalid Login Credentials".
     */
    private void login() {
        try {
            String name = usernameField.getText().trim().toLowerCase();
            Optional<User> user = name.isEmpty() ? Optional.empty() : LibraryStore.get().user(name);
            if (user.isEmpty() || !user.get().checkPassword(passwordField.getText())) {
                loginLabel.setVisible(true);
                return;
            }
            Session.user = user.get();
            if (Session.user.getAdmin()) open("hello-view.fxml", "Library Management System");
            else open("user-screen.fxml", "Library");
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
