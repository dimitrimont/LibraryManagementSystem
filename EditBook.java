package com.library.management.librarymanagementsystem;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Book.java
 * 
 * Defines how to handle book edits
 * 
 * @author Dimitri Montgomery, Hayes Meekins, Preston Beachum, Tyler Gregory, Daniel Irwin
 * Date: 4/22/2025
 */
public class EditBook {
    @FXML
    private TextField editAuthorField;

    @FXML
    private TextField editTitleField;

    @FXML
    private TextField editIsbnField;

    @FXML
    private TextField editCategoryField;

    @FXML
    private Button editSubmitButton;

    @FXML
    private Button editCancelButton;

    private Stage dialogStage;
    private Book book;
    private boolean submitted = false;

    /**
     * sets book objects parameters to values in text fields
     * @param book book object to be edited
     */
    public void setBook(Book book) {
        this.book = book;

        editAuthorField.setText(book.getAuthor());
        editTitleField.setText(book.getTitle());
        editIsbnField.setText(book.getBook_id());
        editCategoryField.setText(book.getGenre());
    }

    /**
     * Submits edits made for book object
     */
    @FXML
    private void handleSubmit() {
        book.setAuthor(editAuthorField.getText());
        book.setTitle(editTitleField.getText());
        book.setBook_id(editIsbnField.getText());
        book.setGenre(editCategoryField.getText());

        submitted = true;
        dialogStage.close();
    }

    /**
     * Cancels edits made and returns to main screen
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    /**
     * Sets stage
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Returns boolean value submitted
     * @return boolean value that shows whether or not changes have been submitted
     */
    public boolean isSubmitted() {
        return submitted;
    }

    /**
     * Checks if book has been submitted, if so it will return the updated book as a String
     * @return String value of updated book
     */
    public String getResult() {
        if (submitted) {
            return book.toCsvString();
        }
        return null;
    }

    /**
     * Loads edit scene
     * @param bookToEdit
     * @return controller for this class
     * @throws IOException
     */
    public static EditBook showDialog(Book bookToEdit) throws IOException {
        FXMLLoader loader = new FXMLLoader(EditBook.class.getResource("edit.fxml"));
        Scene scene = new Scene(loader.load());

        EditBook controller = loader.getController();
        controller.setBook(bookToEdit);

        Stage stage = new Stage();
        stage.setTitle("Edit Book");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);

        controller.setDialogStage(stage);
        stage.showAndWait();

        return controller;
    }
}