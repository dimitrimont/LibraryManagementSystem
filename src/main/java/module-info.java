module com.library.management.librarymanagementsystem {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.library.management.librarymanagementsystem to javafx.fxml;
    exports com.library.management.librarymanagementsystem;
}