package com.library.management.librarymanagementsystem;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

/**
 * Session.java
 *
 * Who is logged in, and the two things every screen does: switch to another screen and show an alert.
 */
final class Session {
    private Session() {}

    /** The logged-in user, or null. */
    static User user;

    static void show(String fxml, String title) throws IOException {
        Parent root = new FXMLLoader(Session.class.getResource(fxml)).load();
        Stage stage = HelloApplication.getPrimaryStage();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.show();
    }

    static void alert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /** Shows what went wrong reading or writing the library's files. */
    static void storageError(IOException e) {
        e.printStackTrace();
        alert(AlertType.ERROR, "Library files", "The library's files could not be read or saved: " + e.getMessage());
    }
}
