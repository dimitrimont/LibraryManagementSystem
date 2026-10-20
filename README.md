# Library Management System

A JavaFX desktop app for a small library. Patrons sign up, search the catalogue, check books out
and return them. An administrator adds, edits and deletes books, can see who has each one, and can
make other accounts administrators.

It was the final project for CSC 331 at UNCW in spring 2025, written by a team of five, and was
revised in October 2026. The tag `first-version` is the Java files as first uploaded here. Commit
`21e2794` is the team's complete final project, from
[prestonbeachum/library-management-system](https://github.com/prestonbeachum/library-management-system).

## Run it

With JDK 21:

```bash
./mvnw javafx:run      # the app
./mvnw test            # 20 tests, headless
```

On Linux the headless tests need GTK's font libraries (`libgtk-3-0`, `libpango-1.0-0`, `fontconfig`).

The library keeps two files in `~/.library-management-system/`, or in the folder given with
`-Dlibrary.data=…`:

- **`books.csv`:** author, title, ISBN, genre and borrower.
- **`users.csv`:** username, then the salt and hash of the password, and whether the account is an
  administrator.

A new library starts with 40 books and no accounts, and the first account created is its
administrator.

## How it works

| Screen | Controller | What it does |
|---|---|---|
| `login-screen.fxml` | `LoginScreenController` | Checks the password; an administrator goes to the catalogue, a patron to the shelf |
| `sign-up-screen.fxml` | `SignUpScreenController` | Creates a patron (or, in a new library, the administrator) |
| `hello-view.fxml` | `HelloViewController` | The whole catalogue with each book's borrower; add, delete, search, make an administrator |
| `edit.fxml` | `editController` | Changes one book |
| `user-screen.fxml` | `UserScreenController` | The books on the shelf; search and check out |
| `return-book.fxml` | `ReturnBookController` | The patron's books; return one |

`LibraryStore` is the only code that reads or writes the files.

- **Format:** standard CSV, with quotes around any field that holds a comma, a quote or a line
  break.
- **Saving:** every save writes a new file and then moves it into place, so a save that fails
  partway leaves the old file whole.
- **Passwords:** stored as PBKDF2-HMAC-SHA256 hashes, with 600,000 iterations and a random 16-byte
  salt for each account.

## Tests

- **`KnownAnswersTest` (15 tests)** drives the real screens through TestFX, with Monocle so no
  display is needed.
  - Each test starts a fresh library: the 40 books, and the accounts ana (patron), admin and ben
    (patron).
  - Each test states the right answer.
  - The first ten are the scenarios the first version was measured on.
- **`StorageTest` (5 tests)** checks the files directly.

`evaluation/run_first_version.sh` runs those ten scenarios against commit `21e2794`, unchanged
except that `module-info.java` is set aside so the tests can run on the class path. The first
version fails 9 of the 10.

## What changed from the first version

- **Anyone could make themselves an administrator.** The sign-up screen had an "Admin" checkbox.
  Now:
  - signing up makes a patron;
  - the first account in a new library is the administrator;
  - an administrator can make other accounts administrators.
- **Checking out the first row deleted every account.** The catalogue listed the file's header row,
  "Author, Title, ISBN, Genre", as a book. Checking it out put an empty book in the patron's
  record. Saving the accounts then failed partway, but only after the file had been emptied. With
  the shipped `users.csv`, which lists a patron first, no accounts were left, the administrator's
  included.
- **A comma in a title locked everyone out.** A patron's record listed their books' fields
  separated by commas. Once a patron borrowed "The Lion, the Witch and the Wardrobe", the account
  file no longer read correctly, and every login failed with "There was a problem reading user
  data".
- **A semicolon in a title damaged the book.** "Code; The Hidden Language" (ISBN 2002, Computing)
  came back from a patron with the title "Code", the ISBN "The Hidden Language" and the genre "2002".
- **A checked-out book disappeared from the catalogue.** Its ISBN could then be given to another
  book, and after the return there were two books with ISBN 1015. Now a checked-out book stays in
  the catalogue with its borrower:
  - the administrator sees who has it, can't reuse its ISBN, and can't delete it until it comes
    back;
  - patrons see only the books on the shelf.
- **Passwords were stored as typed.** An account with no name and no password could also be
  created and used to log in.
- **On a case-sensitive disk, the catalogue was empty.** The code read `inventory.csv`, but the file
  was `Inventory.csv`, so on Linux the administrator saw 0 books. The data was also kept in
  `src/main/resources`, which exists only next to the source code.
- **Smaller fixes:**
  - the sign-up screen showed the password as it was typed;
  - ControlsFX was a dependency that nothing used, and `EditBook.java` was never used;
  - the screens declared JavaFX 23 for a JavaFX 21 app.

## What is still open

- There are no due dates, fines or loan history. The team's planning notes list fines and reports.
- The catalogue holds one copy of each ISBN.
- Two copies of the app open on the same files at once can overwrite each other's changes.

## Credits

Written by Dimitri Montgomery, Hayes Meekins, Preston Beachum, Tyler Gregory and Daniel Irwin, as
the `@author` lines say.
