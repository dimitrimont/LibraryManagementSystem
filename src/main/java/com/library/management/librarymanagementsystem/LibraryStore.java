package com.library.management.librarymanagementsystem;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * LibraryStore.java
 *
 * The one place that reads and writes the library's two files, books.csv and users.csv.
 *
 * They live in a data folder of their own: the system property {@code library.data} if it is set,
 * otherwise {@code .library-management-system} in the user's home folder. (The first version wrote
 * into src/main/resources, which exists only next to the source code, and looked for
 * "inventory.csv" where the file was "Inventory.csv", which only a case-insensitive disk forgives.)
 * On first use the catalogue starts from the 40 books in seed-books.csv; the user list starts empty,
 * and the first account created is the administrator.
 *
 * Every save writes a new file beside the old one and then moves it into place, so a save that
 * fails part-way leaves the old file whole.
 */
public final class LibraryStore {
    static final String BOOKS = "books.csv";
    static final String USERS = "users.csv";
    private static LibraryStore current;

    private final Path dir;

    private LibraryStore(Path dir) {
        this.dir = dir;
    }

    /** The store the app uses. */
    public static synchronized LibraryStore get() {
        if (current == null) {
            String p = System.getProperty("library.data");
            use(p != null ? Paths.get(p) : Paths.get(System.getProperty("user.home"), ".library-management-system"));
        }
        return current;
    }

    /** Points the app at another data folder (the tests use a fresh one each time). */
    public static synchronized void use(Path dir) {
        current = new LibraryStore(dir);
    }

    Path dir() {
        return dir;
    }

    // ---- books --------------------------------------------------------------------------------

    public List<Book> books() throws IOException {
        Path file = dir.resolve(BOOKS);
        if (!Files.exists(file)) {
            try (InputStream seed = LibraryStore.class.getResourceAsStream("seed-books.csv")) {
                Files.createDirectories(dir);
                Files.copy(seed, file);
            }
        }
        List<Book> books = new ArrayList<>();
        int n = 1;
        for (List<String> row : rows(file)) {
            Book b = Book.fromRow(row);
            if (b == null) throw new IOException(BOOKS + " row " + n + " does not have " + Book.HEADER.size() + " fields");
            books.add(b);
            n++;
        }
        return books;
    }

    public void saveBooks(List<Book> books) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        for (Book b : books) rows.add(b.toRow());
        write(BOOKS, Book.HEADER, rows);
    }

    public Optional<Book> book(String id) throws IOException {
        return books().stream().filter(b -> b.getBook_id().equals(id)).findFirst();
    }

    // ---- users --------------------------------------------------------------------------------

    public List<User> users() throws IOException {
        Path file = dir.resolve(USERS);
        List<User> users = new ArrayList<>();
        if (!Files.exists(file)) return users;
        int n = 1;
        for (List<String> row : rows(file)) {
            User u = User.fromRow(row);
            if (u == null) throw new IOException(USERS + " row " + n + " is not a user");
            users.add(u);
            n++;
        }
        return users;
    }

    public void saveUsers(List<User> users) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        for (User u : users) rows.add(u.toRow());
        write(USERS, User.HEADER, rows);
    }

    public Optional<User> user(String username) throws IOException {
        return users().stream().filter(u -> u.getUsername().equals(username)).findFirst();
    }

    // ---- files --------------------------------------------------------------------------------

    /** The file's rows after its header. */
    private static List<List<String>> rows(Path file) throws IOException {
        List<List<String>> rows = Csv.parse(Files.readString(file, StandardCharsets.UTF_8));
        return rows.isEmpty() ? rows : rows.subList(1, rows.size());
    }

    private void write(String name, List<String> header, List<List<String>> rows) throws IOException {
        Files.createDirectories(dir);
        StringBuilder sb = new StringBuilder(Csv.line(header)).append('\n');
        for (List<String> row : rows) sb.append(Csv.line(row)).append('\n');
        Path tmp = Files.createTempFile(dir, name, ".tmp");
        try {
            Files.writeString(tmp, sb, StandardCharsets.UTF_8);
            Files.move(tmp, dir.resolve(name), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(tmp);
        }
    }
}
