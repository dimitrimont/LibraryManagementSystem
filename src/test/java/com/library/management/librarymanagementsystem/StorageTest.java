package com.library.management.librarymanagementsystem;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** The files themselves: what goes in comes back out, and a new library starts where it should. */
class StorageTest {
    @TempDir
    Path dir;

    @Test
    void awkwardTextSurvivesASaveAndALoad() throws IOException {
        LibraryStore.use(dir);
        List<String> titles = List.of("The Lion, the Witch and the Wardrobe", "Code; The Hidden Language",
                "\"Surely You're Joking, Mr. Feynman!\"", "Two\nlines", "  spaced  ", "");
        List<Book> books = titles.stream().map(t -> new Book("A, B", t, "id-" + t.hashCode(), "g;h", "ana")).toList();
        LibraryStore.get().saveBooks(books);
        List<Book> back = LibraryStore.get().books();
        assertEquals(titles, back.stream().map(Book::getTitle).toList());
        assertTrue(back.stream().allMatch(b -> b.getAuthor().equals("A, B") && b.getGenre().equals("g;h")
                && b.getBorrower().equals("ana")));
    }

    @Test
    void aNewLibraryHasTheFortyBooksAndNoAccounts() throws IOException {
        LibraryStore.use(dir.resolve("new"));
        assertEquals(40, LibraryStore.get().books().size());
        assertTrue(LibraryStore.get().users().isEmpty());
        assertTrue(Files.exists(dir.resolve("new").resolve("books.csv")));
    }

    @Test
    void passwordsAreCheckedNotStored() {
        User u = User.create("ana", "ana-pw", false);
        assertTrue(u.checkPassword("ana-pw"));
        assertFalse(u.checkPassword("ana-pw "));
        assertFalse(String.join(",", u.toRow()).contains("ana-pw"));
        User again = User.fromRow(u.toRow());
        assertNotNull(again);
        assertTrue(again.checkPassword("ana-pw"));
    }

    @Test
    void aDamagedUserFileIsReportedNotSkipped() throws IOException {
        LibraryStore.use(dir);
        LibraryStore.get().saveUsers(List.of(User.create("ana", "ana-pw", false)));
        Files.writeString(dir.resolve("users.csv"), "\nnot,a,user\n", java.nio.file.StandardOpenOption.APPEND);
        IOException e = assertThrows(IOException.class, () -> LibraryStore.get().users());
        assertTrue(e.getMessage().contains("row 2"), e.getMessage());
    }

    @Test
    void savingLeavesNoTemporaryFilesBehind() throws IOException {
        LibraryStore.use(dir);
        LibraryStore.get().saveBooks(LibraryStore.get().books());
        LibraryStore.get().saveUsers(List.of(User.create("ana", "ana-pw", false)));
        try (var files = Files.list(dir)) {
            assertEquals(List.of("books.csv", "users.csv"), files.map(p -> p.getFileName().toString()).sorted().toList());
        }
    }
}
