package com.library.management.librarymanagementsystem;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

/**
 * The first version, driven through its own screens. Each test states the right answer; a failure
 * is the first version getting it wrong. Accounts: ana (patron), admin, ben (patron), in that order,
 * as the shipped users.csv puts the patron first. Books: the shipped 40.
 */
@ExtendWith(ApplicationExtension.class)
class FirstVersionKnownAnswersTest {
    static final Path DIR = Paths.get("src/main/resources");
    static final Path USERS = DIR.resolve("users.csv");
    static final Path BOOKS = DIR.resolve("Inventory.csv");
    static final Path SHIPPED_BOOKS = Paths.get("src/test/resources/Inventory.shipped.csv");
    final List<String> alerts = new ArrayList<>();

    @Start
    void start(Stage stage) throws Exception {
        Files.write(USERS, List.of("ana;ana-pw;false", "admin;admin-pw;true", "ben;ben-pw;false"));
        Files.copy(SHIPPED_BOOKS, BOOKS, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        new HelloApplication().start(stage);
    }

    // ---- helpers ------------------------------------------------------------------------------

    void set(FxRobot r, String id, String text) {
        r.interact(() -> ((TextInputControl) r.lookup(id).query()).setText(text));
    }

    /** Fires a button the way a click does. Not inside interact(): an alert's showAndWait would block it. */
    void press(FxRobot r, String id) {
        ButtonBase b = r.lookup(id).query();
        javafx.application.Platform.runLater(b::fire);
        closeAlerts(r);
    }

    /** Closes any alert that opened, keeping its text. */
    void closeAlerts(FxRobot r) {
        WaitForAsyncUtils.waitForFxEvents();
        for (int pass = 0; pass < 5; pass++) {
            List<Window> open = new ArrayList<>(r.listWindows());
            boolean closed = false;
            for (Window w : open) {
                if (w.getScene() != null && w.getScene().getRoot() instanceof DialogPane pane) {
                    alerts.add(pane.getContentText());
                    r.interact(() -> ((Stage) w).close());
                    closed = true;
                }
            }
            WaitForAsyncUtils.waitForFxEvents();
            if (!closed) return;
        }
    }

    boolean on(FxRobot r, String id) {
        return r.lookup(id).tryQuery().isPresent();
    }

    void login(FxRobot r, String user, String pw) {
        set(r, "#usernameField", user);
        set(r, "#passwordField", pw);
        press(r, "#loginButton");
    }

    @SuppressWarnings("unchecked")
    ListView<String> list(FxRobot r) {
        return (ListView<String>) r.lookup("#bookListView").query();
    }

    void pick(FxRobot r, String containing) {
        ListView<String> l = list(r);
        r.interact(() -> {
            for (int i = 0; i < l.getItems().size(); i++)
                if (l.getItems().get(i).contains(containing)) { l.getSelectionModel().select(i); return; }
            fail("no row contains " + containing + ": " + l.getItems());
        });
    }

    void addBook(FxRobot r, String author, String title, String isbn, String genre) {
        set(r, "#authorNameTextField", author);
        set(r, "#bookNameTextField", title);
        set(r, "#isbnTextField", isbn);
        set(r, "#categoryTextField", genre);
        press(r, "#submitButton");
    }

    void checkout(FxRobot r, String user, String pw, String search, String containing) {
        login(r, user, pw);
        set(r, "#searchTextField", search);
        pick(r, containing);
        press(r, "#checkoutButton");
        press(r, "#logoutButton");
    }

    // ---- known answers ------------------------------------------------------------------------

    @Test
    void theAdminSeesAll40Books(FxRobot r) {
        login(r, "admin", "admin-pw");
        assertTrue(on(r, "#deleteButton"), "admin screen");
        assertEquals(40, list(r).getItems().size(), "books listed: " + list(r).getItems().size());
    }

    @Test
    void theCatalogueListsOnlyBooks(FxRobot r) {
        login(r, "ana", "ana-pw");
        assertFalse(list(r).getItems().stream().anyMatch(s -> s.startsWith("Author")),
                "the file's header is listed as a book: " + list(r).getItems().get(0));
    }

    @Test
    void checkingOutTheFirstRowKeepsEveryAccount(FxRobot r) throws Exception {
        login(r, "ana", "ana-pw");
        r.interact(() -> list(r).getSelectionModel().select(0));
        press(r, "#checkoutButton");
        List<String> after = Files.readAllLines(USERS);
        assertEquals(3, after.size(), "accounts left in users.csv: " + after.size() + " " + after);
    }

    @Test
    void signingUpCannotMakeAnAdmin(FxRobot r) {
        press(r, "#createAccountButton");
        set(r, "#usernameTextField", "mallory");
        set(r, "#passwordTextField", "mallory-pw");
        if (on(r, "#isAdmin")) r.interact(() -> ((CheckBox) r.lookup("#isAdmin").query()).setSelected(true));
        press(r, "#createAccountButton");
        login(r, "mallory", "mallory-pw");
        assertFalse(on(r, "#deleteButton"), "a new sign-up reached the admin screen");
    }

    @Test
    void passwordsAreNotStoredAsTyped(FxRobot r) throws Exception {
        press(r, "#createAccountButton");
        set(r, "#usernameTextField", "carol");
        set(r, "#passwordTextField", "carol-secret-pw");
        press(r, "#createAccountButton");
        assertFalse(Files.readString(USERS).contains("carol-secret-pw"), "users.csv holds the password as typed");
    }

    @Test
    void anEmptyAccountIsRefused(FxRobot r) {
        press(r, "#createAccountButton");
        press(r, "#createAccountButton");
        if (on(r, "#cancelButton")) press(r, "#cancelButton");
        login(r, "", "");
        assertTrue(on(r, "#loginButton"), "logged in with no name and no password");
    }

    @Test
    void aCommaInATitleLocksNobodyOut(FxRobot r) {
        login(r, "admin", "admin-pw");
        addBook(r, "C. S. Lewis", "The Lion, the Witch and the Wardrobe", "2001", "Fantasy");
        press(r, "#logoutButton");
        checkout(r, "ana", "ana-pw", "Lion", "Lion");
        login(r, "admin", "admin-pw");
        assertTrue(on(r, "#deleteButton"), "the admin could not log in; alerts: " + alerts);
    }

    @Test
    void aBookComesBackFromAPatronUnchanged(FxRobot r) {
        login(r, "admin", "admin-pw");
        addBook(r, "Charles Petzold", "Code; The Hidden Language", "2002", "Computing");
        press(r, "#logoutButton");
        checkout(r, "ana", "ana-pw", "Petzold", "Petzold");
        login(r, "ana", "ana-pw");
        press(r, "#returnButton");
        pick(r, "Petzold");
        press(r, "#submitButton");
        press(r, "#logoutButton");
        login(r, "admin", "admin-pw");
        set(r, "#searchTextField", "Petzold");
        pick(r, "Petzold");
        press(r, "#editButton");
        String title = ((TextField) r.lookup("#editTitleTextField").query()).getText();
        String isbn = ((TextField) r.lookup("#editISBNTextField").query()).getText();
        String genre = ((TextField) r.lookup("#editCategoryTextField").query()).getText();
        assertEquals("Code; The Hidden Language|2002|Computing", title + "|" + isbn + "|" + genre);
    }

    @Test
    void aCheckedOutBooksIsbnCannotBeReused(FxRobot r) {
        checkout(r, "ana", "ana-pw", "Crime", "Crime and Punishment");
        login(r, "admin", "admin-pw");
        addBook(r, "Someone Else", "Another Book", "1015", "Mystery");
        boolean refused = alerts.stream().anyMatch(a -> a.contains("ISBN"));
        press(r, "#logoutButton");
        login(r, "ana", "ana-pw");
        press(r, "#returnButton");
        pick(r, "Crime");
        press(r, "#submitButton");
        press(r, "#logoutButton");
        login(r, "admin", "admin-pw");
        set(r, "#searchTextField", "1015");
        long copies = list(r).getItems().stream().filter(s -> s.contains("1015")).count();
        assertTrue(refused && copies == 1, "refused: " + refused + ", books with ISBN 1015 afterwards: " + copies);
    }

    @Test
    void aWrongPasswordIsRefused(FxRobot r) {
        login(r, "ana", "not-her-password");
        assertTrue(on(r, "#loginButton") && !on(r, "#checkoutButton"));
    }
}
