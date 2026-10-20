package com.library.management.librarymanagementsystem;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

/**
 * Known-answer tests, run through the app's own screens without a display (see pom.xml).
 *
 * Each test starts a fresh library in its own folder: the 40 shipped books, and three accounts in
 * the order the first version's users.csv put them, a patron first: ana (patron), admin, ben
 * (patron). Each states the right answer. The first ten are the scenarios the first version was
 * measured on; it failed nine of them (see README).
 */
@ExtendWith(ApplicationExtension.class)
class KnownAnswersTest {
    @TempDir
    Path dir;
    final List<String> alerts = new ArrayList<>();

    @Start
    void start(Stage stage) throws Exception {
        LibraryStore.use(dir);
        LibraryStore.get().saveUsers(List.of(
                User.create("ana", "ana-pw", false), User.create("admin", "admin-pw", true), User.create("ben", "ben-pw", false)));
        new HelloApplication().start(stage);
    }

    // ---- helpers ------------------------------------------------------------------------------

    void set(FxRobot r, String id, String text) {
        r.interact(() -> ((TextInputControl) r.lookup(id).query()).setText(text));
    }

    /** Fires a button the way a click does. Not inside interact(): an alert's showAndWait would block it. */
    void press(FxRobot r, String id) {
        ButtonBase b = r.lookup(id).query();
        Platform.runLater(b::fire);
        closeAlerts(r);
    }

    /** Closes any alert that opened, keeping its text. */
    void closeAlerts(FxRobot r) {
        WaitForAsyncUtils.waitForFxEvents();
        for (int pass = 0; pass < 5; pass++) {
            boolean closed = false;
            for (Window w : new ArrayList<>(r.listWindows())) {
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

    void signUp(FxRobot r, String user, String pw) {
        press(r, "#createAccountButton");
        set(r, "#usernameTextField", user);
        set(r, "#passwordTextField", pw);
        press(r, "#createAccountButton");
        if (on(r, "#cancelButton")) press(r, "#cancelButton");
    }

    @SuppressWarnings("unchecked")
    ListView<Book> list(FxRobot r) {
        return (ListView<Book>) r.lookup("#bookListView").query();
    }

    List<String> rows(FxRobot r) {
        return list(r).getItems().stream().map(Book::toString).toList();
    }

    void pick(FxRobot r, String containing) {
        ListView<Book> l = list(r);
        r.interact(() -> {
            for (int i = 0; i < l.getItems().size(); i++)
                if (l.getItems().get(i).toString().contains(containing)) { l.getSelectionModel().select(i); return; }
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

    void giveBack(FxRobot r, String user, String pw, String containing) {
        login(r, user, pw);
        press(r, "#returnButton");
        pick(r, containing);
        press(r, "#submitButton");
        press(r, "#logoutButton");
    }

    // ---- the scenarios the first version was measured on ----------------------------------------

    @Test
    void theAdminSeesAll40Books(FxRobot r) {
        login(r, "admin", "admin-pw");
        assertTrue(on(r, "#deleteButton"), "admin screen");
        assertEquals(40, list(r).getItems().size());
    }

    @Test
    void theCatalogueListsOnlyBooks(FxRobot r) {
        login(r, "ana", "ana-pw");
        assertEquals(40, list(r).getItems().size());
        assertFalse(rows(r).stream().anyMatch(s -> s.startsWith("Author") || s.startsWith("author")));
    }

    @Test
    void checkingOutTheFirstRowKeepsEveryAccount(FxRobot r) throws Exception {
        login(r, "ana", "ana-pw");
        r.interact(() -> list(r).getSelectionModel().select(0));
        press(r, "#checkoutButton");
        assertEquals(3, LibraryStore.get().users().size());
        assertEquals("ana", LibraryStore.get().books().get(0).getBorrower());
    }

    @Test
    void signingUpCannotMakeAnAdmin(FxRobot r) {
        assertFalse(on(r, "#isAdmin"));
        signUp(r, "mallory", "mallory-pw");
        login(r, "mallory", "mallory-pw");
        assertTrue(on(r, "#checkoutButton"), "patron screen");
        assertFalse(on(r, "#deleteButton"), "a new sign-up reached the admin screen");
    }

    @Test
    void passwordsAreNotStoredAsTyped(FxRobot r) throws Exception {
        signUp(r, "carol", "carol-secret-pw");
        assertTrue(LibraryStore.get().user("carol").isPresent());
        assertFalse(Files.readString(dir.resolve(LibraryStore.USERS)).contains("carol-secret-pw"));
    }

    @Test
    void anEmptyAccountIsRefused(FxRobot r) throws Exception {
        signUp(r, "", "");
        assertTrue(alerts.contains("Enter a username and a password."), "alerts: " + alerts);
        assertEquals(3, LibraryStore.get().users().size());
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
        set(r, "#searchTextField", "Lion");
        assertEquals(List.of("C. S. Lewis, The Lion, the Witch and the Wardrobe, ISBN 2001, Fantasy (checked out by ana)"), rows(r));
    }

    @Test
    void aBookComesBackFromAPatronUnchanged(FxRobot r) {
        login(r, "admin", "admin-pw");
        addBook(r, "Charles Petzold", "Code; The Hidden Language", "2002", "Computing");
        press(r, "#logoutButton");
        checkout(r, "ana", "ana-pw", "Petzold", "Petzold");
        giveBack(r, "ana", "ana-pw", "Petzold");
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
        assertTrue(alerts.stream().anyMatch(a -> a.contains("ISBN") && a.contains("checked out")), "alerts: " + alerts);
        press(r, "#logoutButton");
        giveBack(r, "ana", "ana-pw", "Crime");
        login(r, "admin", "admin-pw");
        set(r, "#searchTextField", "1015");
        assertEquals(1, rows(r).size(), "books with ISBN 1015: " + rows(r));
    }

    @Test
    void aWrongPasswordIsRefused(FxRobot r) {
        login(r, "ana", "not-her-password");
        assertTrue(on(r, "#loginButton") && !on(r, "#checkoutButton"));
    }

    // ---- what the revision added ----------------------------------------------------------------

    @Test
    void theFirstAccountInANewLibraryIsTheAdministrator(FxRobot r) throws Exception {
        Files.delete(dir.resolve(LibraryStore.USERS)); // a library with no accounts yet
        signUp(r, "librarian", "librarian-pw");
        signUp(r, "pat", "pat-pw");
        login(r, "librarian", "librarian-pw");
        assertTrue(on(r, "#deleteButton"), "the first account is the administrator");
        press(r, "#logoutButton");
        login(r, "pat", "pat-pw");
        assertTrue(on(r, "#checkoutButton") && !on(r, "#deleteButton"), "the second is a patron");
    }

    @Test
    void anAdministratorCanMakeAnotherOne(FxRobot r) {
        login(r, "admin", "admin-pw");
        set(r, "#promoteTextField", "ben");
        press(r, "#promoteButton");
        press(r, "#logoutButton");
        login(r, "ben", "ben-pw");
        assertTrue(on(r, "#deleteButton"));
    }

    @Test
    void theAdminSeesWhoHasABookAndCannotDeleteIt(FxRobot r) throws Exception {
        checkout(r, "ana", "ana-pw", "Dracula", "Dracula");
        login(r, "admin", "admin-pw");
        set(r, "#searchTextField", "Dracula");
        assertEquals(List.of("Bram Stoker, Dracula, ISBN 1018, Horror (checked out by ana)"), rows(r));
        pick(r, "Dracula");
        press(r, "#deleteButton");
        assertTrue(alerts.stream().anyMatch(a -> a.startsWith("ana has this book")), "alerts: " + alerts);
        assertTrue(LibraryStore.get().book("1018").isPresent());
    }

    @Test
    void aPatronSeesOnlyBooksOnTheShelf(FxRobot r) {
        checkout(r, "ana", "ana-pw", "Dracula", "Dracula");
        login(r, "ben", "ben-pw");
        assertEquals(39, list(r).getItems().size());
        assertFalse(rows(r).stream().anyMatch(s -> s.contains("Dracula")));
    }

    @Test
    void returningWithNothingSelectedChangesNothing(FxRobot r) throws Exception {
        checkout(r, "ana", "ana-pw", "Dracula", "Dracula");
        login(r, "ana", "ana-pw");
        press(r, "#returnButton");
        press(r, "#submitButton");
        assertTrue(alerts.contains("Please select a book to return."), "alerts: " + alerts);
        assertEquals("ana", LibraryStore.get().book("1018").orElseThrow().getBorrower());
    }
}
