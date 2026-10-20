module com.library.management.librarymanagementsystem {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens com.library.management.librarymanagementsystem to javafx.fxml;
    exports com.library.management.librarymanagementsystem;
}