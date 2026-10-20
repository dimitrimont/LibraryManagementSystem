package com.library.management.librarymanagementsystem;

import java.io.IOException;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * SignUpScreenController.java
 *
 * Controller file for the sign-up-screen.fxml. Provides functionality to the control elements within the GUI.
 * Signing up makes a patron. The one exception is a new library with no accounts yet: its first
 * account is the administrator, who can then make other accounts administrators.
 *
 * @author Dimitri Montgomery, Hayes Meekins, Preston Beachum, Tyler Gregory, Daniel Irwin
 * Date: 4/22/2025, revised October 2026
 */
public class SignUpScreenController {

    @FXML
    private Button cancelButton;

    @FXML
    private Button createAccountButton;

    @FXML
    private PasswordField passwordTextField;

    @FXML
    private TextField usernameTextField;

    /**
     * Calls all methods needed to initialize scene
     */
    @FXML
    public void initialize() {
        createAccountButton.setOnAction(event -> createAccount());
        cancelButton.setOnAction(event -> switchToLogin());
    }

    /**
     * Adds a patron, or the administrator if this is the library's first account, and goes back to login.
     */
    private void createAccount() {
        String name = usernameTextField.getText().trim().toLowerCase();
        String password = passwordTextField.getText();
        if (name.isEmpty() || password.isEmpty()) {
            Session.alert(AlertType.WARNING, "Account Creation Failed", "Enter a username and a password.");
            return;
        }
        if (!name.matches("[a-z0-9._-]{1,32}")) {
            Session.alert(AlertType.WARNING, "Account Creation Failed",
                    "A username can have up to 32 letters, digits, dots, dashes and underscores.");
            return;
        }
        try {
            List<User> users = LibraryStore.get().users();
            if (users.stream().anyMatch(u -> u.getUsername().equals(name))) {
                Session.alert(AlertType.INFORMATION, "Account Creation Failed", "Username is taken. Please try again");
                return;
            }
            boolean first = users.isEmpty();
            users.add(User.create(name, password, first));
            LibraryStore.get().saveUsers(users);
            if (first) {
                Session.alert(AlertType.INFORMATION, "Administrator",
                        "This is the library's first account, so it is the administrator.");
            }
            switchToLogin();
        } catch (IOException e) {
            Session.storageError(e);
        }
    }

    /**
     * Changes scene back to login
     */
    private void switchToLogin() {
        try {
            Session.show("login-screen.fxml", "Library");
        } catch (IOException e) {
            Session.storageError(e);
        }
    }
}
