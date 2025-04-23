package com.library.management.librarymanagementsystem;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * HelloApplication.java
 * 
 * Creates and loads the first scene for JavaFx program
 * 
 * @author Dimitri Montgomery, Hayes Meekins, Preston Beachum, Tyler Gregory, Daniel Irwin
 * Date: 4/22/2025
 */
public class HelloApplication extends Application {
    private static Stage primaryStage;

    /**
     * Loads the first scene of the program
     * @param stage
     * @throws IOException
     */
    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-screen.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Library Management System");
        stage.setScene(scene);
        stage.show();
    }
    /**
     * Returns primary stage instance variable
     * @return primaryStage instance variable
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Launches javafx
     * @param args
     */
    public static void main(String[] args) {
        launch();
    }
}
