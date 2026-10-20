---
title: "Library Management System"
subtitle: "A Command-Line Based Management System in Java with SQLite Database Connectivity"
date: "October 2026"
---

# 1. Introduction

The **Library Management System (LMS)** is a menu-driven, command-line
application written in Java. It helps a small library — for example a
school, college or community library — keep track of its **books**, its
**members**, and the **loans** (book issues and returns) between them.

The librarian runs the program from the Windows Command Prompt or any
terminal. A main menu leads to three management areas (Books, Members, Issue
& Return) and a summary report. Every operation is a simple numbered choice,
and every value the user types is checked before it is accepted.

All data is stored in an **SQLite** relational database accessed through
**JDBC**. SQLite is embedded in the application, so no separate database
server has to be installed; the whole library is kept in a single file
(`library.db`) that is created automatically the first time the program runs
and is loaded again on every later start. Records therefore remain available
after the program is closed and restarted.

The project is organised in layers — model, data access (DAO), service
(business rules) and user interface — so that each class has one clear
responsibility. It demonstrates the core Java and Object-Oriented
Programming concepts covered in the course: multiple interconnected classes,
encapsulation, inheritance, abstraction (abstract classes and interfaces),
polymorphism, modularisation, loops and conditional statements, exception
handling, and database connectivity with full CRUD operations.

# 2. Problem Statement

Many small libraries still record books and borrowings in paper registers
or loose spreadsheets. This causes several practical problems:

* **No reliable availability information** – staff cannot quickly tell how
  many copies of a title are on the shelf and how many are on loan.
* **Duplicate and inconsistent records** – the same book or member may be
  entered twice, and typing mistakes (wrong e-mail, invalid ISBN, impossible
  year) go unnoticed.
* **Lost track of borrowed books** – it is hard to see who has which book,
  which loans are overdue, and how much fine is owed.
* **No enforcement of rules** – borrowing limits and loan periods for
  different kinds of members are applied by hand, if at all.
* **Fragile storage** – paper registers and ad-hoc files are easily lost,
  and searching them is slow.

The library therefore needs a simple, dependable computer system that keeps
all records in a database, validates every input, enforces the library's
borrowing rules automatically, and can be operated by staff without any
graphical interface or special hardware.

# 3. Objectives

1. Design a **menu-driven command-line interface** that lets a librarian
   manage the library from CMD / a terminal without a GUI.
2. Store all books, members and loans in a **relational database (SQLite)**
   using **JDBC**, so the data **persists** across program runs.
3. Implement complete **CRUD operations** – Create, Read, Update and Delete –
   for books and members, and for loan records.
4. Automate the library workflow: **issue and return books**, keep the
   number of available copies up to date, enforce **borrowing limits and
   loan periods** per membership type, detect **overdue loans** and
   **calculate fines**.
5. Apply **Object-Oriented Programming**: multiple interconnected classes,
   encapsulation with getters/setters, inheritance, abstraction through
   abstract classes and interfaces, and polymorphism.
6. Organise the code into **modules (packages)** with a clear separation
   between the user interface, business logic and data access.
7. **Validate user input** and **handle errors** gracefully: invalid menu
   selections, wrong data formats, duplicate records, missing records and
   database connection failures, without the program crashing.
8. Keep the database **consistent** by using constraints (primary keys,
   unique keys, foreign keys) and transactions for operations that change
   more than one table.

# 4. Tools and Libraries Used

| Category | Tool / Library | Purpose |
|---|---|---|
| Programming language | **Java SE 17+** (tested with OpenJDK 21) | Application code |
| IDE / Editor | IntelliJ IDEA Community / VS Code (any Java IDE works) | Writing and debugging code |
| Build tool | **Apache Maven 3.9** | Compiling, dependency download, building the runnable JAR |
| Database | **SQLite 3** (embedded) | Persistent relational storage in `library.db` |
| JDBC driver | **sqlite-jdbc 3.46.1.3** (`org.xerial`) | Connects Java to SQLite through the standard JDBC API |
| Logging binding | slf4j-nop 2.0.16 | Silences the driver's optional logging output |
| Maven plugin | maven-shade-plugin 3.6.0 | Packages the app and driver into one executable JAR |
| Interface | Windows Command Prompt / Linux & macOS terminal | Running the program |
| Version control | Git & GitHub | Source code management |

**Java standard library APIs used**

* `java.sql` – `Connection`, `DriverManager`, `PreparedStatement`,
  `ResultSet`, `Statement`, `SQLException` (JDBC database access, transactions).
* `java.util` – `Scanner` (console input), `List`, `ArrayList`,
  `LinkedHashMap`, `Optional`, `regex.Pattern` (input validation),
  `function.Predicate`.
* `java.time` – `LocalDate`, `Year`, `ChronoUnit` (issue/due dates and fines).

# 5. Code Implementation and Output

## 5.1 System design

The application is divided into eight packages. Each layer only talks to
the layer directly below it:

```
            +------------------------------------------+
  UI        |  MainMenu  BookMenu  MemberMenu  LoanMenu |  (menus, input)
            |        Menu (abstract)  InputReader       |
            +---------------------+--------------------+
                                  |
            +---------------------v--------------------+
  Service   |  BookService  MemberService  LoanService |  (business rules)
            +---------------------+--------------------+
                                  |
            +---------------------v--------------------+
  DAO       |  BookDao  MemberDao  LoanDao              |  (SQL, CRUD)
            |  AbstractDao (abstract) <- CrudDao<T>     |
            +---------------------+--------------------+
                                  |
            +---------------------v--------------------+
  Database  |  DatabaseManager (JDBC, singleton)       |---> library.db (SQLite)
            +------------------------------------------+

  Model:      Entity (abstract) <- Person (abstract) <- Member
                                <- Book
                                <- Loan               MembershipType (enum)
  Exceptions: LibraryException <- ValidationException, DuplicateRecordException,
                                  RecordNotFoundException, DataAccessException
```

**Database schema** (created automatically by `DatabaseManager`):

| Table | Columns | Constraints |
|---|---|---|
| `books` | id, isbn, title, author, genre, publication_year, total_copies, available_copies | PK `id`, **UNIQUE** `isbn`, CHECK copies ≥ 0 |
| `members` | id, name, email, phone, membership_type, join_date | PK `id`, **UNIQUE** `email` (case-insensitive) |
| `loans` | id, book_id, member_id, issue_date, due_date, return_date | PK `id`, **FOREIGN KEYS** to books and members |

**Library rules implemented**

| Membership | Max. books at a time | Loan period |
|---|---|---|
| STUDENT | 3 | 14 days |
| FACULTY | 10 | 30 days |
| PUBLIC | 2 | 7 days |

Late returns are charged **Rs. 10 per day**.

## 5.2 How the project meets each requirement

| Requirement | Where it is implemented |
|---|---|
| **Multiple classes** | 31 Java source files (classes, interfaces and an enum) in 8 packages: `app`, `db`, `model`, `dao`, `service`, `ui`, `util`, `exception`. The menus use the services, the services use the DAOs, the DAOs use `DatabaseManager`. |
| **Encapsulation** | All fields are `private` (e.g. `Book.isbn`, `Member.membershipType`, `DatabaseManager.connection`) and are reached through public getters/setters. `DatabaseManager` has a private constructor (singleton); `Validator`, `ConsoleUtil` have private constructors. |
| **Inheritance & polymorphism** | `Member` → `Person` → `Entity`; `Book`, `Loan` → `Entity`. `validate()` is overridden in every model and called polymorphically; `getRole()` is abstract in `Person`. `BookDao`, `MemberDao`, `LoanDao` extend `AbstractDao` and override `mapRow()`, `tableName()`, `selectSql()`. All menus extend `Menu`. |
| **Abstraction** | Abstract classes `Entity`, `Person`, `AbstractDao`, `Menu`; generic interface `CrudDao<T>`; functional interfaces `Menu.Action` and `DatabaseManager.Transaction`. The UI never sees SQL; services never see `ResultSet`s. |
| **Modularisation** | Each feature is its own class and method, e.g. `LoanService.issueBook()`, `BookMenu.updateBook()`, `TablePrinter.print()`, `InputReader.readInt()`. |
| **Loops & conditionals** | `while` loop in `Menu.show()` keeps the menu running until 0 is chosen; `while(true)` input loops in `InputReader` repeat until valid; a `do-while` loop re-asks for a valid ISBN when editing a book; `for`/for-each loops build tables and totals; `if/else if/else` chains select the menu action and check limits, availability, duplicates and overdue status. |
| **CLI interface** | `MainMenu`, `BookMenu`, `MemberMenu`, `LoanMenu` display numbered menus, headings and aligned tables. |
| **Database connectivity** | `DatabaseManager` uses `DriverManager.getConnection("jdbc:sqlite:library.db")`; all queries use `PreparedStatement` (protection against SQL injection). |
| **CRUD** | Create: `BookDao.create`, `MemberDao.create`, `LoanDao.create`. Read: `findAll`, `findById`, `search`, `findActive`, `findOverdue`. Update: `BookDao.update`, `MemberDao.update`, `LoanDao.update`. Delete: `AbstractDao.delete`. |
| **Data persistence** | Data is written to `library.db`; Figure 16 shows the same records after restarting the program. |
| **Validation & error handling** | `Validator` (ISBN, e-mail, phone, ranges), `InputReader` (numbers, dates, y/n, menu choices), custom exceptions (`ValidationException`, `DuplicateRecordException`, `RecordNotFoundException`, `DataAccessException`), transactions with rollback, friendly message on connection failure (Figure 17). |

## 5.3 How to compile and run

```
mvn package
java -jar target/library-management-system.jar
```

On Windows the included `run.bat` does the same. The database file
`library.db` is created in the current folder on the first run.

## 5.4 Program output (screenshots)

The following screenshots were captured from real runs of the program.
Where the same menu is printed again between two operations, it has been
replaced by a "⋮ (menu redisplayed – omitted)" line to keep the figures short.
To demonstrate overdue fines, the dates of loan #1 were moved 20 days into
the past directly in the database before the run shown in Figure 11
(simulating the passing of time).

![Figure 1: Program start-up, main menu and Book Management menu](screenshots/fig01_startup_menu.png)

![Figure 2: Create: adding books (with invalid ISBN and invalid year rejected)](screenshots/fig02_add_book.png)

![Figure 3: Input validation and duplicate-ISBN detection](screenshots/fig03_add_book_validation.png)

![Figure 4: Read: listing all books stored in the database](screenshots/fig04_view_books.png)

![Figure 5: Create: registering members (invalid e-mail/phone/type rejected, duplicate e-mail detected)](screenshots/fig05_add_member.png)

![Figure 6: Read: listing all members](screenshots/fig06_view_members.png)

![Figure 7: Issuing books - success, no copies left, borrowing limit and unknown ID](screenshots/fig07_issue.png)

![Figure 8: Books currently on loan](screenshots/fig08_active_loans.png)

![Figure 9: Search and Update: editing a book (Enter keeps the current value)](screenshots/fig09_search_update_book.png)

![Figure 10: Update: changing a member's e-mail and membership type](screenshots/fig10_update_member.png)

![Figure 11: Overdue list, returning books with late-fine calculation, and double-return prevention](screenshots/fig11_return.png)

![Figure 12: Extending a due date, member history and full loan list](screenshots/fig12_due_history.png)

![Figure 13: Error handling: invalid menu selections](screenshots/fig13_invalid_choice.png)

![Figure 14: Delete: cancelled, refused (copies on loan) and confirmed deletion](screenshots/fig14_delete_book.png)

![Figure 15: Delete member, summary report and exit](screenshots/fig15_delete_member_summary.png)

![Figure 16: Data persistence: records are still available after restarting the program](screenshots/fig16_persistence.png)

![Figure 17: Error handling: database connection failure (database path not accessible)](screenshots/fig17_db_failure.png)


# 6. Conclusion

This project produced a complete, working Library Management System that
runs entirely in the command line and stores its data in an SQLite database
through JDBC. The librarian can add, view, search, update and delete books
and members, issue and return books, follow overdue loans and fines, and
see a summary of the library – and every record is still there the next
time the program starts.

**What I learned**

* **Object-Oriented design.** Splitting the program into small classes with
  one job each (models, DAOs, services, menus) made it much easier to write,
  test and change. Inheritance (`Member` → `Person` → `Entity`) and abstract
  classes (`AbstractDao`, `Menu`) removed a lot of repeated code, and
  polymorphism lets the same call (`validate()`, `mapRow()`, `show()`) behave
  correctly for each type.
* **Encapsulation and abstraction.** Keeping fields private and exposing them
  through getters/setters protects objects from invalid changes. Hiding SQL
  behind the `CrudDao` interface means the menus do not need to know anything
  about the database – the storage could be switched to MySQL by changing
  only the data layer.
* **Database connectivity with JDBC.** I learned how to open a connection,
  create tables, use `PreparedStatement` with parameters (which also prevents
  SQL injection), read `ResultSet`s into objects, and use **transactions**
  (`commit` / `rollback`) so that issuing or returning a book updates both
  the `loans` and `books` tables together or not at all.
* **Validation and error handling.** Real users make mistakes. Re-asking for
  input in loops, checking formats with regular expressions, using custom
  exception classes, and catching errors at the menu level keeps the program
  from ever crashing and always tells the user what went wrong.
* **Control flow.** Loops drive the menus and input prompts, and conditional
  statements implement the library's rules (borrowing limits, availability,
  overdue checks, fines).

**How the project demonstrates the key concepts**

The system uses 31 interconnected classes, interfaces and enums organised
in eight packages; it applies encapsulation, inheritance, abstraction and
polymorphism; it is fully menu-driven with loops and conditionals; it
connects to a real relational database through JDBC; it implements all four
CRUD operations; its data persists between runs; and it validates input and
handles invalid menu choices, wrong formats, duplicate records, missing
records and database connection failures.

**Possible future improvements** include librarian login with passwords,
reservations for books that are currently on loan, e-mail reminders for
overdue books, exporting reports to CSV/PDF, and an optional graphical or web
front-end that reuses the same service and DAO layers.

# Appendix A – Complete Java Source Code

The complete source code of the project is listed below, package by package.

## Package `com.library.app`

### Main.java

```java
package com.library.app;

import com.library.dao.BookDao;
import com.library.dao.LoanDao;
import com.library.dao.MemberDao;
import com.library.db.DatabaseManager;
import com.library.exception.DataAccessException;
import com.library.service.BookService;
import com.library.service.LoanService;
import com.library.service.MemberService;
import com.library.ui.ConsoleUtil;
import com.library.ui.InputClosedException;
import com.library.ui.InputReader;
import com.library.ui.MainMenu;

import java.util.Scanner;

/**
 * Entry point of the Library Management System.
 * Connects to the database, wires all layers together and starts the main menu.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        DatabaseManager db = DatabaseManager.getInstance();
        try {
            db.connect();
        } catch (DataAccessException e) {
            System.err.println("FATAL: " + e.getMessage());
            System.err.println("Check that the folder is writable and the SQLite JDBC driver is on the classpath.");
            System.exit(1);
            return;
        }

        BookDao bookDao = new BookDao(db);
        MemberDao memberDao = new MemberDao(db);
        LoanDao loanDao = new LoanDao(db);

        BookService bookService = new BookService(db, bookDao, loanDao);
        MemberService memberService = new MemberService(db, memberDao, loanDao);
        LoanService loanService = new LoanService(db, bookDao, memberDao, loanDao);

        ConsoleUtil.banner("WELCOME TO THE CITY LIBRARY");
        System.out.println("  Connected to database: " + db.getUrl());

        try (Scanner scanner = new Scanner(System.in)) {
            InputReader in = new InputReader(scanner);
            new MainMenu(in, bookService, memberService, loanService).show();
        } catch (InputClosedException e) {
            System.out.println();
            System.out.println("Input closed.");
        } finally {
            db.close();
        }
        System.out.println("All records have been saved. Goodbye!");
    }
}
```

## Package `com.library.db`

### DatabaseManager.java

```java
package com.library.db;

import com.library.exception.DataAccessException;
import com.library.exception.LibraryException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Owns the single JDBC connection used by the application (Singleton pattern)
 * and creates the database schema the first time the program runs.
 *
 * <p>SQLite keeps all data in one file, so every record survives after the
 * program is closed and is loaded again on the next start.</p>
 */
public final class DatabaseManager {

    public static final String DEFAULT_DB_FILE = "library.db";

    private static DatabaseManager instance;

    private final String url;
    private Connection connection;

    private DatabaseManager(String dbFile) {
        this.url = "jdbc:sqlite:" + dbFile;
    }

    /** Returns the shared manager, creating it on first use. */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            String dbFile = System.getProperty("library.db", DEFAULT_DB_FILE);
            instance = new DatabaseManager(dbFile);
        }
        return instance;
    }

    /** Opens the connection (if needed) and makes sure all tables exist. */
    public void connect() throws DataAccessException {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url);
                try (Statement st = connection.createStatement()) {
                    // SQLite only enforces foreign keys when this pragma is switched on.
                    st.execute("PRAGMA foreign_keys = ON");
                }
                createSchema();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not connect to the database (" + url + "): " + e.getMessage(), e);
        }
    }

    public Connection getConnection() throws DataAccessException {
        connect();
        return connection;
    }

    /** A unit of work that must either fully succeed or be rolled back. */
    @FunctionalInterface
    public interface Transaction<R> {
        R execute() throws LibraryException;
    }

    /**
     * Runs several DAO calls as one atomic database transaction:
     * if any step throws, every change made by the earlier steps is undone.
     */
    public <R> R inTransaction(Transaction<R> work) throws LibraryException {
        Connection conn = getConnection();
        try {
            conn.setAutoCommit(false);
            R result = work.execute();
            conn.commit();
            return result;
        } catch (LibraryException | RuntimeException e) {
            rollback(conn);
            throw e;
        } catch (SQLException e) {
            rollback(conn);
            throw new DataAccessException("Transaction failed: " + e.getMessage(), e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {
                // connection is unusable; the next call to connect() will report it
            }
        }
    }

    private static void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            System.err.println("Warning: rollback failed: " + e.getMessage());
        }
    }

    public String getUrl() {
        return url;
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Warning: failed to close database connection: " + e.getMessage());
            }
        }
    }

    private void createSchema() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS books (
                        id               INTEGER PRIMARY KEY AUTOINCREMENT,
                        isbn             TEXT    NOT NULL UNIQUE,
                        title            TEXT    NOT NULL,
                        author           TEXT    NOT NULL,
                        genre            TEXT    NOT NULL,
                        publication_year INTEGER NOT NULL,
                        total_copies     INTEGER NOT NULL CHECK (total_copies > 0),
                        available_copies INTEGER NOT NULL CHECK (available_copies >= 0)
                    )""");
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS members (
                        id              INTEGER PRIMARY KEY AUTOINCREMENT,
                        name            TEXT NOT NULL,
                        email           TEXT NOT NULL UNIQUE COLLATE NOCASE,
                        phone           TEXT NOT NULL,
                        membership_type TEXT NOT NULL,
                        join_date       TEXT NOT NULL
                    )""");
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS loans (
                        id          INTEGER PRIMARY KEY AUTOINCREMENT,
                        book_id     INTEGER NOT NULL REFERENCES books(id),
                        member_id   INTEGER NOT NULL REFERENCES members(id),
                        issue_date  TEXT    NOT NULL,
                        due_date    TEXT    NOT NULL,
                        return_date TEXT
                    )""");
        }
    }
}
```

## Package `com.library.model`

### Book.java

```java
package com.library.model;

import com.library.exception.ValidationException;
import com.library.util.Validator;

import java.time.Year;

/** A book title held by the library, possibly with several physical copies. */
public class Book extends Entity {

    private String isbn;
    private String title;
    private String author;
    private String genre;
    private int publicationYear;
    private int totalCopies;
    private int availableCopies;

    public Book() {
    }

    public Book(String isbn, String title, String author, String genre, int publicationYear, int totalCopies) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.publicationYear = publicationYear;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }

    /** Number of copies that are currently lent out. */
    public int getIssuedCopies() {
        return totalCopies - availableCopies;
    }

    public boolean isAvailable() {
        return availableCopies > 0;
    }

    @Override
    public void validate() throws ValidationException {
        Validator.requireIsbn(isbn);
        Validator.requireNonBlank(title, "Title");
        Validator.requireMaxLength(title, 100, "Title");
        Validator.requireNonBlank(author, "Author");
        Validator.requireMaxLength(author, 60, "Author");
        Validator.requireNonBlank(genre, "Genre");
        Validator.requireMaxLength(genre, 30, "Genre");
        Validator.requireRange(publicationYear, 1450, Year.now().getValue(), "Publication year");
        Validator.requireRange(totalCopies, 1, 1000, "Total copies");
        if (availableCopies < 0 || availableCopies > totalCopies) {
            throw new ValidationException("Available copies must be between 0 and the total number of copies.");
        }
    }

    @Override
    public String toString() {
        return "Book #" + getId() + " - \"" + title + "\" by " + author;
    }
}
```

### Entity.java

```java
package com.library.model;

import com.library.exception.ValidationException;

/**
 * Abstract root of every record stored in the database.
 * It holds the primary key and forces each subclass to provide its own validation rules.
 */
public abstract class Entity {

    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * Checks that the object is in a valid state before it is written to the database.
     *
     * @throws ValidationException if any field is invalid
     */
    public abstract void validate() throws ValidationException;
}
```

### Loan.java

```java
package com.library.model;

import com.library.exception.ValidationException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Records that a member borrowed a book, and when (or whether) it was returned. */
public class Loan extends Entity {

    /** Late fee charged for every day a book is overdue. */
    public static final double FINE_PER_DAY = 10.0;

    private int bookId;
    private int memberId;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;

    // Read-only display helpers filled in by the DAO through SQL joins.
    private String bookTitle;
    private String memberName;

    public Loan() {
    }

    public Loan(int bookId, int memberId, LocalDate issueDate, LocalDate dueDate) {
        this.bookId = bookId;
        this.memberId = memberId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public boolean isReturned() {
        return returnDate != null;
    }

    /** A loan is overdue if it is still open after its due date, or was returned late. */
    public boolean isOverdue() {
        LocalDate reference = isReturned() ? returnDate : LocalDate.now();
        return reference.isAfter(dueDate);
    }

    public long getDaysOverdue() {
        if (!isOverdue()) {
            return 0;
        }
        LocalDate reference = isReturned() ? returnDate : LocalDate.now();
        return ChronoUnit.DAYS.between(dueDate, reference);
    }

    public double calculateFine() {
        return getDaysOverdue() * FINE_PER_DAY;
    }

    public String getStatus() {
        if (isReturned()) {
            return isOverdue() ? "RETURNED LATE" : "RETURNED";
        }
        return isOverdue() ? "OVERDUE" : "ISSUED";
    }

    @Override
    public void validate() throws ValidationException {
        if (bookId <= 0 || memberId <= 0) {
            throw new ValidationException("A loan must reference a valid book and member.");
        }
        if (issueDate == null || dueDate == null) {
            throw new ValidationException("Issue date and due date are required.");
        }
        if (dueDate.isBefore(issueDate)) {
            throw new ValidationException("Due date cannot be before the issue date.");
        }
        if (returnDate != null && returnDate.isBefore(issueDate)) {
            throw new ValidationException("Return date cannot be before the issue date.");
        }
    }
}
```

### Member.java

```java
package com.library.model;

import com.library.exception.ValidationException;

import java.time.LocalDate;

/** A registered library member who is allowed to borrow books. */
public class Member extends Person {

    private MembershipType membershipType;
    private LocalDate joinDate;

    public Member() {
    }

    public Member(String name, String email, String phone, MembershipType membershipType) {
        super(name, email, phone);
        this.membershipType = membershipType;
        this.joinDate = LocalDate.now();
    }

    public MembershipType getMembershipType() {
        return membershipType;
    }

    public void setMembershipType(MembershipType membershipType) {
        this.membershipType = membershipType;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    @Override
    public String getRole() {
        return membershipType + " member";
    }

    @Override
    public void validate() throws ValidationException {
        super.validate();
        if (membershipType == null) {
            throw new ValidationException("Membership type is required.");
        }
    }

    @Override
    public String toString() {
        return "Member #" + getId() + " - " + getName() + " (" + getRole() + ")";
    }
}
```

### MembershipType.java

```java
package com.library.model;

/**
 * The kinds of membership the library offers.
 * Every type carries its own borrowing limit and loan period.
 */
public enum MembershipType {
    STUDENT(3, 14),
    FACULTY(10, 30),
    PUBLIC(2, 7);

    private final int borrowLimit;
    private final int loanDays;

    MembershipType(int borrowLimit, int loanDays) {
        this.borrowLimit = borrowLimit;
        this.loanDays = loanDays;
    }

    public int getBorrowLimit() {
        return borrowLimit;
    }

    public int getLoanDays() {
        return loanDays;
    }
}
```

### Person.java

```java
package com.library.model;

import com.library.exception.ValidationException;
import com.library.util.Validator;

/**
 * Abstract description of a person known to the library.
 * Common contact data lives here; concrete subclasses add role specific data.
 */
public abstract class Person extends Entity {

    private String name;
    private String email;
    private String phone;

    protected Person() {
    }

    protected Person(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    /** Short human readable description of the person's role (implemented by subclasses). */
    public abstract String getRole();

    @Override
    public void validate() throws ValidationException {
        Validator.requireNonBlank(name, "Name");
        Validator.requireMaxLength(name, 60, "Name");
        Validator.requireEmail(email);
        Validator.requirePhone(phone);
    }
}
```

## Package `com.library.exception`

### DataAccessException.java

```java
package com.library.exception;

/**
 * Wraps low-level {@link java.sql.SQLException}s so that the upper layers
 * never depend on JDBC classes directly (abstraction of the persistence layer).
 */
public class DataAccessException extends LibraryException {

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### DuplicateRecordException.java

```java
package com.library.exception;

/** Thrown when inserting/updating a record would violate a uniqueness rule (e.g. same ISBN or e-mail). */
public class DuplicateRecordException extends LibraryException {

    public DuplicateRecordException(String message) {
        super(message);
    }
}
```

### LibraryException.java

```java
package com.library.exception;

/**
 * Base class for all application-specific (checked) exceptions.
 * Having a common parent lets the UI layer catch every "expected" error in one place.
 */
public class LibraryException extends Exception {

    public LibraryException(String message) {
        super(message);
    }

    public LibraryException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### RecordNotFoundException.java

```java
package com.library.exception;

/** Thrown when a record with the requested ID does not exist in the database. */
public class RecordNotFoundException extends LibraryException {

    public RecordNotFoundException(String entityName, int id) {
        super(entityName + " with ID " + id + " was not found.");
    }
}
```

### ValidationException.java

```java
package com.library.exception;

/** Thrown when a value entered by the user breaks a business or format rule. */
public class ValidationException extends LibraryException {

    public ValidationException(String message) {
        super(message);
    }
}
```

## Package `com.library.dao`

### AbstractDao.java

```java
package com.library.dao;

import com.library.db.DatabaseManager;
import com.library.exception.DataAccessException;
import com.library.exception.LibraryException;
import com.library.exception.RecordNotFoundException;
import com.library.model.Entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Shares the JDBC plumbing that is identical for every table
 * (finding by ID, listing, deleting, mapping result sets) so that the
 * concrete DAOs only describe what is specific to their entity.
 */
public abstract class AbstractDao<T extends Entity> implements CrudDao<T> {

    protected final DatabaseManager db;

    protected AbstractDao(DatabaseManager db) {
        this.db = db;
    }

    /** Name of the database table. */
    protected abstract String tableName();

    /** Converts the current row of a result set into an entity object. */
    protected abstract T mapRow(ResultSet rs) throws SQLException;

    /** SELECT used for reading records; subclasses may override it to add joins. */
    protected String selectSql() {
        return "SELECT * FROM " + tableName();
    }

    /** Column used for ordering lists. */
    protected String idColumn() {
        return "id";
    }

    protected Connection connection() throws DataAccessException {
        return db.getConnection();
    }

    @Override
    public Optional<T> findById(int id) throws LibraryException {
        List<T> result = query(selectSql() + " WHERE " + idColumn() + " = ?", id);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public List<T> findAll() throws LibraryException {
        return query(selectSql() + " ORDER BY " + idColumn());
    }

    @Override
    public void delete(int id) throws LibraryException {
        int rows = executeUpdate("DELETE FROM " + tableName() + " WHERE id = ?", id);
        if (rows == 0) {
            throw new RecordNotFoundException(entityName(), id);
        }
    }

    /** Runs a SELECT with positional parameters and maps every row. */
    protected List<T> query(String sql, Object... params) throws DataAccessException {
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to read " + entityName().toLowerCase() + " records: " + e.getMessage(), e);
        }
    }

    /** Runs a SELECT that returns a single number, such as COUNT(*). */
    protected int queryInt(String sql, Object... params) throws DataAccessException {
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database query failed: " + e.getMessage(), e);
        }
    }

    /** Runs INSERT/UPDATE/DELETE and returns the number of affected rows. */
    protected int executeUpdate(String sql, Object... params) throws DataAccessException {
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save " + entityName().toLowerCase() + ": " + e.getMessage(), e);
        }
    }

    /** Runs an INSERT and returns the auto-generated primary key. */
    protected int executeInsert(String sql, Object... params) throws DataAccessException {
        try (PreparedStatement ps = connection().prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            bind(ps, params);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new DataAccessException("No ID was generated for the new " + entityName().toLowerCase() + ".", null);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to add " + entityName().toLowerCase() + ": " + e.getMessage(), e);
        }
    }

    private static void bind(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }
}
```

### BookDao.java

```java
package com.library.dao;

import com.library.db.DatabaseManager;
import com.library.exception.LibraryException;
import com.library.exception.RecordNotFoundException;
import com.library.model.Book;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** Data access object for the {@code books} table. */
public class BookDao extends AbstractDao<Book> {

    public BookDao(DatabaseManager db) {
        super(db);
    }

    @Override
    public String entityName() {
        return "Book";
    }

    @Override
    protected String tableName() {
        return "books";
    }

    @Override
    protected Book mapRow(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getInt("id"));
        book.setIsbn(rs.getString("isbn"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setGenre(rs.getString("genre"));
        book.setPublicationYear(rs.getInt("publication_year"));
        book.setTotalCopies(rs.getInt("total_copies"));
        book.setAvailableCopies(rs.getInt("available_copies"));
        return book;
    }

    @Override
    public Book create(Book book) throws LibraryException {
        int id = executeInsert("""
                INSERT INTO books (isbn, title, author, genre, publication_year, total_copies, available_copies)
                VALUES (?, ?, ?, ?, ?, ?, ?)""",
                book.getIsbn(), book.getTitle(), book.getAuthor(), book.getGenre(),
                book.getPublicationYear(), book.getTotalCopies(), book.getAvailableCopies());
        book.setId(id);
        return book;
    }

    @Override
    public void update(Book book) throws LibraryException {
        int rows = executeUpdate("""
                UPDATE books SET isbn = ?, title = ?, author = ?, genre = ?,
                       publication_year = ?, total_copies = ?, available_copies = ?
                WHERE id = ?""",
                book.getIsbn(), book.getTitle(), book.getAuthor(), book.getGenre(),
                book.getPublicationYear(), book.getTotalCopies(), book.getAvailableCopies(), book.getId());
        if (rows == 0) {
            throw new RecordNotFoundException(entityName(), book.getId());
        }
    }

    public Optional<Book> findByIsbn(String isbn) throws LibraryException {
        List<Book> result = query(selectSql() + " WHERE isbn = ?", isbn);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    /** Case-insensitive search in title, author and genre. */
    public List<Book> search(String keyword) throws LibraryException {
        String pattern = "%" + keyword.toLowerCase() + "%";
        return query(selectSql()
                        + " WHERE lower(title) LIKE ? OR lower(author) LIKE ? OR lower(genre) LIKE ? ORDER BY title",
                pattern, pattern, pattern);
    }
}
```

### CrudDao.java

```java
package com.library.dao;

import com.library.exception.LibraryException;
import com.library.exception.RecordNotFoundException;
import com.library.model.Entity;

import java.util.List;
import java.util.Optional;

/**
 * Generic contract for Create, Read, Update and Delete operations.
 * Each concrete DAO hides the SQL needed for one table behind these methods.
 *
 * @param <T> the entity type handled by the DAO
 */
public interface CrudDao<T extends Entity> {

    /** Inserts a new record and returns it with its generated ID. */
    T create(T entity) throws LibraryException;

    Optional<T> findById(int id) throws LibraryException;

    List<T> findAll() throws LibraryException;

    /** Saves changes to an existing record. */
    void update(T entity) throws LibraryException;

    /** Deletes the record with the given ID. */
    void delete(int id) throws LibraryException;

    /** Convenience method: like {@link #findById(int)} but fails if nothing is found. */
    default T getById(int id) throws LibraryException {
        return findById(id).orElseThrow(() -> new RecordNotFoundException(entityName(), id));
    }

    /** Name used in user-facing messages, e.g. "Book". */
    String entityName();
}
```

### LoanDao.java

```java
package com.library.dao;

import com.library.db.DatabaseManager;
import com.library.exception.LibraryException;
import com.library.exception.RecordNotFoundException;
import com.library.model.Loan;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Data access object for the {@code loans} table. */
public class LoanDao extends AbstractDao<Loan> {

    public LoanDao(DatabaseManager db) {
        super(db);
    }

    @Override
    public String entityName() {
        return "Loan";
    }

    @Override
    protected String tableName() {
        return "loans";
    }

    @Override
    protected String idColumn() {
        return "l.id";
    }

    /** Joins the book and member tables so a loan can be shown with readable names. */
    @Override
    protected String selectSql() {
        return """
                SELECT l.*, b.title AS book_title, m.name AS member_name
                FROM loans l
                JOIN books b   ON b.id = l.book_id
                JOIN members m ON m.id = l.member_id""";
    }

    @Override
    protected Loan mapRow(ResultSet rs) throws SQLException {
        Loan loan = new Loan();
        loan.setId(rs.getInt("id"));
        loan.setBookId(rs.getInt("book_id"));
        loan.setMemberId(rs.getInt("member_id"));
        loan.setIssueDate(LocalDate.parse(rs.getString("issue_date")));
        loan.setDueDate(LocalDate.parse(rs.getString("due_date")));
        String returned = rs.getString("return_date");
        loan.setReturnDate(returned == null ? null : LocalDate.parse(returned));
        loan.setBookTitle(rs.getString("book_title"));
        loan.setMemberName(rs.getString("member_name"));
        return loan;
    }

    @Override
    public Loan create(Loan loan) throws LibraryException {
        int id = executeInsert("""
                INSERT INTO loans (book_id, member_id, issue_date, due_date, return_date)
                VALUES (?, ?, ?, ?, ?)""",
                loan.getBookId(), loan.getMemberId(), loan.getIssueDate().toString(),
                loan.getDueDate().toString(), toText(loan.getReturnDate()));
        loan.setId(id);
        return loan;
    }

    @Override
    public void update(Loan loan) throws LibraryException {
        int rows = executeUpdate("""
                UPDATE loans SET book_id = ?, member_id = ?, issue_date = ?, due_date = ?, return_date = ?
                WHERE id = ?""",
                loan.getBookId(), loan.getMemberId(), loan.getIssueDate().toString(),
                loan.getDueDate().toString(), toText(loan.getReturnDate()), loan.getId());
        if (rows == 0) {
            throw new RecordNotFoundException(entityName(), loan.getId());
        }
    }

    public List<Loan> findActive() throws LibraryException {
        return query(selectSql() + " WHERE l.return_date IS NULL ORDER BY l.due_date");
    }

    public List<Loan> findOverdue() throws LibraryException {
        return query(selectSql() + " WHERE l.return_date IS NULL AND l.due_date < ? ORDER BY l.due_date",
                LocalDate.now().toString());
    }

    public List<Loan> findByMember(int memberId) throws LibraryException {
        return query(selectSql() + " WHERE l.member_id = ? ORDER BY l.issue_date DESC", memberId);
    }

    public int countActiveByMember(int memberId) throws LibraryException {
        return queryInt("SELECT COUNT(*) FROM loans WHERE member_id = ? AND return_date IS NULL", memberId);
    }

    public int countActiveByBook(int bookId) throws LibraryException {
        return queryInt("SELECT COUNT(*) FROM loans WHERE book_id = ? AND return_date IS NULL", bookId);
    }

    public boolean hasActiveLoan(int memberId, int bookId) throws LibraryException {
        return queryInt("SELECT COUNT(*) FROM loans WHERE member_id = ? AND book_id = ? AND return_date IS NULL",
                memberId, bookId) > 0;
    }

    /** Removes the loan history of a book (used before the book itself is deleted). */
    public void deleteByBook(int bookId) throws LibraryException {
        executeUpdate("DELETE FROM loans WHERE book_id = ?", bookId);
    }

    /** Removes the loan history of a member (used before the member is deleted). */
    public void deleteByMember(int memberId) throws LibraryException {
        executeUpdate("DELETE FROM loans WHERE member_id = ?", memberId);
    }

    private static String toText(LocalDate date) {
        return date == null ? null : date.toString();
    }
}
```

### MemberDao.java

```java
package com.library.dao;

import com.library.db.DatabaseManager;
import com.library.exception.LibraryException;
import com.library.exception.RecordNotFoundException;
import com.library.model.Member;
import com.library.model.MembershipType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Data access object for the {@code members} table. */
public class MemberDao extends AbstractDao<Member> {

    public MemberDao(DatabaseManager db) {
        super(db);
    }

    @Override
    public String entityName() {
        return "Member";
    }

    @Override
    protected String tableName() {
        return "members";
    }

    @Override
    protected Member mapRow(ResultSet rs) throws SQLException {
        Member member = new Member();
        member.setId(rs.getInt("id"));
        member.setName(rs.getString("name"));
        member.setEmail(rs.getString("email"));
        member.setPhone(rs.getString("phone"));
        member.setMembershipType(MembershipType.valueOf(rs.getString("membership_type")));
        member.setJoinDate(LocalDate.parse(rs.getString("join_date")));
        return member;
    }

    @Override
    public Member create(Member member) throws LibraryException {
        int id = executeInsert("""
                INSERT INTO members (name, email, phone, membership_type, join_date)
                VALUES (?, ?, ?, ?, ?)""",
                member.getName(), member.getEmail(), member.getPhone(),
                member.getMembershipType().name(), member.getJoinDate().toString());
        member.setId(id);
        return member;
    }

    @Override
    public void update(Member member) throws LibraryException {
        int rows = executeUpdate("""
                UPDATE members SET name = ?, email = ?, phone = ?, membership_type = ?
                WHERE id = ?""",
                member.getName(), member.getEmail(), member.getPhone(),
                member.getMembershipType().name(), member.getId());
        if (rows == 0) {
            throw new RecordNotFoundException(entityName(), member.getId());
        }
    }

    public Optional<Member> findByEmail(String email) throws LibraryException {
        List<Member> result = query(selectSql() + " WHERE email = ? COLLATE NOCASE", email);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public List<Member> searchByName(String keyword) throws LibraryException {
        return query(selectSql() + " WHERE lower(name) LIKE ? ORDER BY name", "%" + keyword.toLowerCase() + "%");
    }
}
```

## Package `com.library.service`

### BookService.java

```java
package com.library.service;

import com.library.dao.BookDao;
import com.library.dao.LoanDao;
import com.library.db.DatabaseManager;
import com.library.exception.DuplicateRecordException;
import com.library.exception.LibraryException;
import com.library.exception.ValidationException;
import com.library.model.Book;
import com.library.util.Validator;

import java.util.List;
import java.util.Optional;

/** Business rules for managing the book catalogue. */
public class BookService {

    private final DatabaseManager db;
    private final BookDao bookDao;
    private final LoanDao loanDao;

    public BookService(DatabaseManager db, BookDao bookDao, LoanDao loanDao) {
        this.db = db;
        this.bookDao = bookDao;
        this.loanDao = loanDao;
    }

    public Book addBook(Book book) throws LibraryException {
        book.setIsbn(Validator.normalizeIsbn(book.getIsbn()));
        book.validate();
        ensureIsbnIsNew(book.getIsbn());
        return bookDao.create(book);
    }

    /** Fails early (before the user types the remaining fields) if the ISBN is already in the catalogue. */
    public void ensureIsbnIsNew(String isbn) throws LibraryException {
        Optional<Book> existing = bookDao.findByIsbn(Validator.normalizeIsbn(isbn));
        if (existing.isPresent()) {
            throw new DuplicateRecordException("A book with ISBN " + existing.get().getIsbn()
                    + " already exists (" + existing.get() + ").");
        }
    }

    public List<Book> listBooks() throws LibraryException {
        return bookDao.findAll();
    }

    public Book getBook(int id) throws LibraryException {
        return bookDao.getById(id);
    }

    public List<Book> searchBooks(String keyword) throws LibraryException {
        Validator.requireNonBlank(keyword, "Search keyword");
        return bookDao.search(keyword.trim());
    }

    /**
     * Saves the edited book. When the total number of copies changes, the
     * available copies are adjusted so that copies already on loan stay counted.
     */
    public void updateBook(Book book) throws LibraryException {
        book.setIsbn(Validator.normalizeIsbn(book.getIsbn()));
        int issued = loanDao.countActiveByBook(book.getId());
        if (book.getTotalCopies() < issued) {
            throw new ValidationException("Total copies cannot be less than the " + issued
                    + " copy/copies currently on loan.");
        }
        book.setAvailableCopies(book.getTotalCopies() - issued);
        book.validate();

        Optional<Book> sameIsbn = bookDao.findByIsbn(book.getIsbn());
        if (sameIsbn.isPresent() && sameIsbn.get().getId() != book.getId()) {
            throw new DuplicateRecordException("Another book already uses ISBN " + book.getIsbn() + ".");
        }
        bookDao.update(book);
    }

    /** Deletes a book together with its (finished) loan history. */
    public void deleteBook(int id) throws LibraryException {
        Book book = bookDao.getById(id);
        if (loanDao.countActiveByBook(id) > 0) {
            throw new ValidationException("\"" + book.getTitle()
                    + "\" cannot be deleted while copies are still on loan.");
        }
        db.inTransaction(() -> {
            loanDao.deleteByBook(id);
            bookDao.delete(id);
            return null;
        });
    }
}
```

### LoanService.java

```java
package com.library.service;

import com.library.dao.BookDao;
import com.library.dao.LoanDao;
import com.library.dao.MemberDao;
import com.library.db.DatabaseManager;
import com.library.exception.LibraryException;
import com.library.exception.ValidationException;
import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.Member;

import java.time.LocalDate;
import java.util.List;

/**
 * Handles issuing and returning books. Each operation changes two tables
 * (loans and books), so both changes are made inside one transaction.
 */
public class LoanService {

    private final DatabaseManager db;
    private final BookDao bookDao;
    private final MemberDao memberDao;
    private final LoanDao loanDao;

    public LoanService(DatabaseManager db, BookDao bookDao, MemberDao memberDao, LoanDao loanDao) {
        this.db = db;
        this.bookDao = bookDao;
        this.memberDao = memberDao;
        this.loanDao = loanDao;
    }

    public Loan issueBook(int bookId, int memberId) throws LibraryException {
        Book book = bookDao.getById(bookId);
        Member member = memberDao.getById(memberId);

        if (!book.isAvailable()) {
            throw new ValidationException("No copies of \"" + book.getTitle() + "\" are available right now.");
        }
        int limit = member.getMembershipType().getBorrowLimit();
        if (loanDao.countActiveByMember(memberId) >= limit) {
            throw new ValidationException(member.getName() + " has reached the borrowing limit of " + limit + " books.");
        }
        if (loanDao.hasActiveLoan(memberId, bookId)) {
            throw new ValidationException(member.getName() + " already has a copy of \"" + book.getTitle() + "\".");
        }

        LocalDate today = LocalDate.now();
        Loan loan = new Loan(bookId, memberId, today, today.plusDays(member.getMembershipType().getLoanDays()));
        loan.validate();

        return db.inTransaction(() -> {
            book.setAvailableCopies(book.getAvailableCopies() - 1);
            bookDao.update(book);
            Loan saved = loanDao.create(loan);
            saved.setBookTitle(book.getTitle());
            saved.setMemberName(member.getName());
            return saved;
        });
    }

    /** Marks a loan as returned and puts the copy back on the shelf. */
    public Loan returnBook(int loanId) throws LibraryException {
        Loan loan = loanDao.getById(loanId);
        if (loan.isReturned()) {
            throw new ValidationException("Loan #" + loanId + " was already returned on " + loan.getReturnDate() + ".");
        }
        loan.setReturnDate(LocalDate.now());

        return db.inTransaction(() -> {
            Book book = bookDao.getById(loan.getBookId());
            book.setAvailableCopies(Math.min(book.getTotalCopies(), book.getAvailableCopies() + 1));
            bookDao.update(book);
            loanDao.update(loan);
            return loan;
        });
    }

    /** Lets the librarian extend (or correct) the due date of an open loan. */
    public void changeDueDate(int loanId, LocalDate newDueDate) throws LibraryException {
        Loan loan = loanDao.getById(loanId);
        if (loan.isReturned()) {
            throw new ValidationException("The due date of a returned loan cannot be changed.");
        }
        loan.setDueDate(newDueDate);
        loan.validate();
        loanDao.update(loan);
    }

    /** Deletes a loan record. Open loans give their copy back to the book. */
    public void deleteLoan(int loanId) throws LibraryException {
        Loan loan = loanDao.getById(loanId);
        db.inTransaction(() -> {
            if (!loan.isReturned()) {
                Book book = bookDao.getById(loan.getBookId());
                book.setAvailableCopies(Math.min(book.getTotalCopies(), book.getAvailableCopies() + 1));
                bookDao.update(book);
            }
            loanDao.delete(loanId);
            return null;
        });
    }

    public List<Loan> listAllLoans() throws LibraryException {
        return loanDao.findAll();
    }

    public List<Loan> listActiveLoans() throws LibraryException {
        return loanDao.findActive();
    }

    public List<Loan> listOverdueLoans() throws LibraryException {
        return loanDao.findOverdue();
    }

    public List<Loan> loansOfMember(int memberId) throws LibraryException {
        memberDao.getById(memberId); // fails with a clear message if the member does not exist
        return loanDao.findByMember(memberId);
    }
}
```

### MemberService.java

```java
package com.library.service;

import com.library.dao.LoanDao;
import com.library.dao.MemberDao;
import com.library.db.DatabaseManager;
import com.library.exception.DuplicateRecordException;
import com.library.exception.LibraryException;
import com.library.exception.ValidationException;
import com.library.model.Member;

import java.util.List;
import java.util.Optional;

/** Business rules for managing library members. */
public class MemberService {

    private final DatabaseManager db;
    private final MemberDao memberDao;
    private final LoanDao loanDao;

    public MemberService(DatabaseManager db, MemberDao memberDao, LoanDao loanDao) {
        this.db = db;
        this.memberDao = memberDao;
        this.loanDao = loanDao;
    }

    public Member registerMember(Member member) throws LibraryException {
        member.validate();
        ensureEmailIsFree(member);
        return memberDao.create(member);
    }

    /** Fails early if the e-mail address already belongs to another member. */
    public void ensureEmailIsNew(String email) throws LibraryException {
        Member probe = new Member();
        probe.setEmail(email);
        ensureEmailIsFree(probe);
    }

    public List<Member> listMembers() throws LibraryException {
        return memberDao.findAll();
    }

    public Member getMember(int id) throws LibraryException {
        return memberDao.getById(id);
    }

    public List<Member> searchMembers(String keyword) throws LibraryException {
        if (keyword == null || keyword.isBlank()) {
            throw new ValidationException("Search keyword cannot be empty.");
        }
        return memberDao.searchByName(keyword.trim());
    }

    public void updateMember(Member member) throws LibraryException {
        member.validate();
        int active = loanDao.countActiveByMember(member.getId());
        if (active > member.getMembershipType().getBorrowLimit()) {
            throw new ValidationException("The member currently has " + active + " books on loan, which exceeds the "
                    + member.getMembershipType() + " limit of " + member.getMembershipType().getBorrowLimit() + ".");
        }
        ensureEmailIsFree(member);
        memberDao.update(member);
    }

    /** Deletes a member together with their (finished) loan history. */
    public void deleteMember(int id) throws LibraryException {
        Member member = memberDao.getById(id);
        if (loanDao.countActiveByMember(id) > 0) {
            throw new ValidationException(member.getName() + " must return all borrowed books before being removed.");
        }
        db.inTransaction(() -> {
            loanDao.deleteByMember(id);
            memberDao.delete(id);
            return null;
        });
    }

    private void ensureEmailIsFree(Member member) throws LibraryException {
        Optional<Member> existing = memberDao.findByEmail(member.getEmail());
        if (existing.isPresent() && existing.get().getId() != member.getId()) {
            throw new DuplicateRecordException("The e-mail " + member.getEmail() + " is already registered to "
                    + existing.get().getName() + ".");
        }
    }
}
```

## Package `com.library.ui`

### BookMenu.java

```java
package com.library.ui;

import com.library.exception.LibraryException;
import com.library.model.Book;
import com.library.service.BookService;
import com.library.util.TablePrinter;
import com.library.util.Validator;

import java.time.Year;
import java.util.List;

/** Console screens for creating, reading, updating and deleting books. */
public class BookMenu extends Menu {

    private static final String ISBN_RULE = "ISBN must have 10 or 13 digits (hyphens are allowed).";

    private final BookService service;

    public BookMenu(InputReader in, BookService service) {
        super(in, "BOOK MANAGEMENT", "Back to main menu");
        this.service = service;
        option("Add a new book", this::addBook);
        option("View all books", this::listBooks);
        option("Search books (title / author / genre)", this::searchBooks);
        option("View book details", this::viewBook);
        option("Update a book", this::updateBook);
        option("Delete a book", this::deleteBook);
    }

    private void addBook() throws LibraryException {
        int currentYear = Year.now().getValue();
        String isbn = in.readMatching("ISBN             : ", Validator::isValidIsbn, ISBN_RULE);
        service.ensureIsbnIsNew(isbn);
        String title = in.readRequired("Title            : ");
        String author = in.readRequired("Author           : ");
        String genre = in.readRequired("Genre            : ");
        int year = in.readInt("Publication year : ", 1450, currentYear);
        int copies = in.readInt("Number of copies : ", 1, 1000);

        Book book = service.addBook(new Book(isbn, title, author, genre, year, copies));
        ConsoleUtil.success("Book added successfully with ID " + book.getId() + ".");
    }

    private void listBooks() throws LibraryException {
        printBooks(service.listBooks());
    }

    private void searchBooks() throws LibraryException {
        String keyword = in.readRequired("Enter keyword: ");
        printBooks(service.searchBooks(keyword));
    }

    private void viewBook() throws LibraryException {
        Book book = service.getBook(in.readId("Enter book ID: "));
        System.out.println();
        System.out.println("  ID               : " + book.getId());
        System.out.println("  ISBN             : " + book.getIsbn());
        System.out.println("  Title            : " + book.getTitle());
        System.out.println("  Author           : " + book.getAuthor());
        System.out.println("  Genre            : " + book.getGenre());
        System.out.println("  Publication year : " + book.getPublicationYear());
        System.out.println("  Total copies     : " + book.getTotalCopies());
        System.out.println("  Available copies : " + book.getAvailableCopies());
        System.out.println("  On loan          : " + book.getIssuedCopies());
    }

    private void updateBook() throws LibraryException {
        Book book = service.getBook(in.readId("Enter ID of the book to update: "));
        System.out.println("Editing " + book + ". Press Enter to keep the current value.");

        String isbn;
        do {
            isbn = in.readOptional("ISBN", book.getIsbn());
            if (!Validator.isValidIsbn(isbn)) {
                System.out.println("  [!] " + ISBN_RULE);
            }
        } while (!Validator.isValidIsbn(isbn));
        book.setIsbn(isbn);
        book.setTitle(in.readOptional("Title", book.getTitle()));
        book.setAuthor(in.readOptional("Author", book.getAuthor()));
        book.setGenre(in.readOptional("Genre", book.getGenre()));
        book.setPublicationYear(in.readOptionalInt("Publication year", book.getPublicationYear(),
                1450, Year.now().getValue()));
        book.setTotalCopies(in.readOptionalInt("Total copies", book.getTotalCopies(), 1, 1000));

        service.updateBook(book);
        ConsoleUtil.success("Book #" + book.getId() + " updated successfully.");
    }

    private void deleteBook() throws LibraryException {
        Book book = service.getBook(in.readId("Enter ID of the book to delete: "));
        if (in.confirm("Delete " + book + "?")) {
            service.deleteBook(book.getId());
            ConsoleUtil.success("Book deleted.");
        } else {
            ConsoleUtil.info("Deletion cancelled.");
        }
    }

    static void printBooks(List<Book> books) {
        if (books.isEmpty()) {
            ConsoleUtil.info("No books found.");
            return;
        }
        TablePrinter table = new TablePrinter("ID", "ISBN", "Title", "Author", "Genre", "Year", "Avail/Total");
        for (Book b : books) {
            table.addRow(b.getId(), b.getIsbn(), b.getTitle(), b.getAuthor(), b.getGenre(),
                    b.getPublicationYear(), b.getAvailableCopies() + "/" + b.getTotalCopies());
        }
        table.print();
    }
}
```

### ConsoleUtil.java

```java
package com.library.ui;

/** Small helpers for consistent console formatting. */
public final class ConsoleUtil {

    public static final int WIDTH = 56;
    public static final String LINE = "=".repeat(WIDTH);

    private ConsoleUtil() {
    }

    public static void banner(String title) {
        System.out.println(LINE);
        int padding = Math.max(0, (WIDTH - title.length()) / 2);
        System.out.println(" ".repeat(padding) + title);
        System.out.println(LINE);
    }

    public static void heading(String text) {
        System.out.println();
        System.out.println("--- " + text + " " + "-".repeat(Math.max(3, WIDTH - text.length() - 5)));
    }

    public static void success(String message) {
        System.out.println("  [OK] " + message);
    }

    public static void info(String message) {
        System.out.println("  " + message);
    }
}
```

### InputClosedException.java

```java
package com.library.ui;

/** Signals that standard input was closed (e.g. Ctrl+D / Ctrl+Z), so the program should end. */
public class InputClosedException extends RuntimeException {

    public InputClosedException() {
        super("Input stream closed.");
    }
}
```

### InputReader.java

```java
package com.library.ui;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import java.util.function.Predicate;

/**
 * Reads and validates everything the user types.
 * Each method keeps asking until a correctly formatted value is entered,
 * so the rest of the program never has to deal with bad console input.
 */
public class InputReader {

    private final Scanner scanner;

    public InputReader(Scanner scanner) {
        this.scanner = scanner;
    }

    /** Reads one raw line (trimmed). */
    public String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            throw new InputClosedException();
        }
        return scanner.nextLine().trim();
    }

    public String readRequired(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (!value.isEmpty()) {
                return value;
            }
            error("This field cannot be empty.");
        }
    }

    /** Reads text that must satisfy a rule; {@code message} explains the rule. */
    public String readMatching(String prompt, Predicate<String> rule, String message) {
        while (true) {
            String value = readRequired(prompt);
            if (rule.test(value)) {
                return value;
            }
            error(message);
        }
    }

    public int readInt(String prompt) {
        while (true) {
            String value = readLine(prompt);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                error("'" + value + "' is not a whole number. Please try again.");
            }
        }
    }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            error("Please enter a number between " + min + " and " + max + ".");
        }
    }

    /** Reads a positive record ID. */
    public int readId(String prompt) {
        return readInt(prompt, 1, Integer.MAX_VALUE);
    }

    public LocalDate readDate(String prompt) {
        while (true) {
            String value = readLine(prompt);
            try {
                return LocalDate.parse(value);
            } catch (DateTimeParseException e) {
                error("Invalid date. Use the format YYYY-MM-DD, e.g. " + LocalDate.now() + ".");
            }
        }
    }

    // ----- "edit" variants: pressing Enter keeps the current value -----

    public String readOptional(String label, String current) {
        String value = readLine(label + " [" + current + "]: ");
        return value.isEmpty() ? current : value;
    }

    public int readOptionalInt(String label, int current, int min, int max) {
        while (true) {
            String value = readLine(label + " [" + current + "]: ");
            if (value.isEmpty()) {
                return current;
            }
            try {
                int number = Integer.parseInt(value);
                if (number >= min && number <= max) {
                    return number;
                }
                error("Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                error("'" + value + "' is not a whole number. Please try again.");
            }
        }
    }

    public <E extends Enum<E>> E readEnum(String label, Class<E> type, E current) {
        E[] values = type.getEnumConstants();
        for (int i = 0; i < values.length; i++) {
            System.out.println("   " + (i + 1) + ". " + values[i]);
        }
        String suffix = current == null ? ": " : " [" + current + "]: ";
        while (true) {
            String value = readLine(label + " (1-" + values.length + ")" + suffix);
            if (value.isEmpty() && current != null) {
                return current;
            }
            try {
                int index = Integer.parseInt(value);
                if (index >= 1 && index <= values.length) {
                    return values[index - 1];
                }
            } catch (NumberFormatException ignored) {
                // fall through to the error message below
            }
            error("Please choose a number from 1 to " + values.length + ".");
        }
    }

    public boolean confirm(String question) {
        while (true) {
            String answer = readLine(question + " (y/n): ").toLowerCase();
            if (answer.equals("y") || answer.equals("yes")) {
                return true;
            }
            if (answer.equals("n") || answer.equals("no")) {
                return false;
            }
            error("Please answer 'y' or 'n'.");
        }
    }

    public void pause() {
        readLine("\nPress Enter to continue...");
    }

    private static void error(String message) {
        System.out.println("  [!] " + message);
    }
}
```

### LoanMenu.java

```java
package com.library.ui;

import com.library.exception.LibraryException;
import com.library.model.Loan;
import com.library.service.LoanService;
import com.library.util.TablePrinter;

import java.time.LocalDate;
import java.util.List;

/** Console screens for issuing and returning books and viewing loan records. */
public class LoanMenu extends Menu {

    private final LoanService service;

    public LoanMenu(InputReader in, LoanService service) {
        super(in, "ISSUE & RETURN", "Back to main menu");
        this.service = service;
        option("Issue a book to a member", this::issueBook);
        option("Return a book", this::returnBook);
        option("View all loans", () -> printLoans(service.listAllLoans()));
        option("View books currently on loan", () -> printLoans(service.listActiveLoans()));
        option("View overdue loans", () -> printLoans(service.listOverdueLoans()));
        option("View a member's borrowing history", this::memberHistory);
        option("Extend / change a due date", this::changeDueDate);
        option("Delete a loan record", this::deleteLoan);
    }

    private void issueBook() throws LibraryException {
        int bookId = in.readId("Book ID   : ");
        int memberId = in.readId("Member ID : ");
        Loan loan = service.issueBook(bookId, memberId);
        ConsoleUtil.success("\"" + loan.getBookTitle() + "\" issued to " + loan.getMemberName()
                + " (loan #" + loan.getId() + "). Due back on " + loan.getDueDate() + ".");
    }

    private void returnBook() throws LibraryException {
        Loan loan = service.returnBook(in.readId("Loan ID: "));
        ConsoleUtil.success("\"" + loan.getBookTitle() + "\" returned by " + loan.getMemberName() + ".");
        if (loan.getDaysOverdue() > 0) {
            ConsoleUtil.info(String.format("Returned %d day(s) late. Fine due: Rs. %.2f",
                    loan.getDaysOverdue(), loan.calculateFine()));
        } else {
            ConsoleUtil.info("Returned on time. No fine.");
        }
    }

    private void memberHistory() throws LibraryException {
        printLoans(service.loansOfMember(in.readId("Member ID: ")));
    }

    private void changeDueDate() throws LibraryException {
        int loanId = in.readId("Loan ID: ");
        LocalDate date = in.readDate("New due date (YYYY-MM-DD): ");
        service.changeDueDate(loanId, date);
        ConsoleUtil.success("Due date of loan #" + loanId + " changed to " + date + ".");
    }

    private void deleteLoan() throws LibraryException {
        int loanId = in.readId("Loan ID: ");
        if (in.confirm("Permanently delete loan #" + loanId + "?")) {
            service.deleteLoan(loanId);
            ConsoleUtil.success("Loan record deleted.");
        } else {
            ConsoleUtil.info("Deletion cancelled.");
        }
    }

    static void printLoans(List<Loan> loans) {
        if (loans.isEmpty()) {
            ConsoleUtil.info("No loans found.");
            return;
        }
        TablePrinter table = new TablePrinter("Loan", "Book", "Member", "Issued", "Due", "Returned", "Status", "Fine");
        for (Loan l : loans) {
            table.addRow(l.getId(), l.getBookTitle(), l.getMemberName(), l.getIssueDate(), l.getDueDate(),
                    l.getReturnDate(), l.getStatus(), String.format("%.2f", l.calculateFine()));
        }
        table.print();
    }
}
```

### MainMenu.java

```java
package com.library.ui;

import com.library.exception.LibraryException;
import com.library.model.Book;
import com.library.model.Loan;
import com.library.service.BookService;
import com.library.service.LoanService;
import com.library.service.MemberService;

import java.util.List;

/** Top-level menu that links all the sub-menus together. */
public class MainMenu extends Menu {

    private final BookService bookService;
    private final MemberService memberService;
    private final LoanService loanService;

    public MainMenu(InputReader in, BookService bookService, MemberService memberService, LoanService loanService) {
        super(in, "LIBRARY MANAGEMENT SYSTEM - MAIN MENU", "Exit");
        this.bookService = bookService;
        this.memberService = memberService;
        this.loanService = loanService;

        submenu("Manage books", new BookMenu(in, bookService));
        submenu("Manage members", new MemberMenu(in, memberService));
        submenu("Issue / return books", new LoanMenu(in, loanService));
        option("Library summary report", this::summary);
    }

    private void summary() throws LibraryException {
        List<Book> books = bookService.listBooks();
        List<Loan> active = loanService.listActiveLoans();
        List<Loan> overdue = loanService.listOverdueLoans();

        int totalCopies = 0;
        int available = 0;
        for (Book b : books) {
            totalCopies += b.getTotalCopies();
            available += b.getAvailableCopies();
        }
        double pendingFines = 0;
        for (Loan l : overdue) {
            pendingFines += l.calculateFine();
        }

        System.out.printf("  Book titles          : %d%n", books.size());
        System.out.printf("  Total copies         : %d%n", totalCopies);
        System.out.printf("  Copies on the shelf  : %d%n", available);
        System.out.printf("  Registered members   : %d%n", memberService.listMembers().size());
        System.out.printf("  Books on loan        : %d%n", active.size());
        System.out.printf("  Overdue loans        : %d%n", overdue.size());
        System.out.printf("  Fines accumulating   : Rs. %.2f%n", pendingFines);
    }
}
```

### MemberMenu.java

```java
package com.library.ui;

import com.library.exception.LibraryException;
import com.library.model.Member;
import com.library.model.MembershipType;
import com.library.service.MemberService;
import com.library.util.TablePrinter;
import com.library.util.Validator;

import java.util.List;
import java.util.function.Predicate;

/** Console screens for creating, reading, updating and deleting members. */
public class MemberMenu extends Menu {

    private static final String EMAIL_RULE = "Invalid e-mail address (example: name@example.com).";
    private static final String PHONE_RULE = "Phone number must contain 7-15 digits, optionally starting with '+'.";

    private final MemberService service;

    public MemberMenu(InputReader in, MemberService service) {
        super(in, "MEMBER MANAGEMENT", "Back to main menu");
        this.service = service;
        option("Register a new member", this::addMember);
        option("View all members", this::listMembers);
        option("Search members by name", this::searchMembers);
        option("View member details", this::viewMember);
        option("Update a member", this::updateMember);
        option("Delete a member", this::deleteMember);
    }

    private void addMember() throws LibraryException {
        String name = in.readRequired("Full name : ");
        String email = in.readMatching("E-mail    : ", Validator::isValidEmail, EMAIL_RULE);
        service.ensureEmailIsNew(email);
        String phone = in.readMatching("Phone     : ", Validator::isValidPhone, PHONE_RULE);
        System.out.println("Membership type:");
        MembershipType type = in.readEnum("Choose", MembershipType.class, null);

        Member member = service.registerMember(new Member(name, email, phone, type));
        ConsoleUtil.success("Member registered successfully with ID " + member.getId() + ".");
    }

    private void listMembers() throws LibraryException {
        printMembers(service.listMembers());
    }

    private void searchMembers() throws LibraryException {
        printMembers(service.searchMembers(in.readRequired("Enter part of the name: ")));
    }

    private void viewMember() throws LibraryException {
        Member m = service.getMember(in.readId("Enter member ID: "));
        System.out.println();
        System.out.println("  ID           : " + m.getId());
        System.out.println("  Name         : " + m.getName());
        System.out.println("  E-mail       : " + m.getEmail());
        System.out.println("  Phone        : " + m.getPhone());
        System.out.println("  Membership   : " + m.getMembershipType());
        System.out.println("  Borrow limit : " + m.getMembershipType().getBorrowLimit() + " books");
        System.out.println("  Loan period  : " + m.getMembershipType().getLoanDays() + " days");
        System.out.println("  Joined on    : " + m.getJoinDate());
    }

    private void updateMember() throws LibraryException {
        Member m = service.getMember(in.readId("Enter ID of the member to update: "));
        System.out.println("Editing " + m + ". Press Enter to keep the current value.");

        m.setName(in.readOptional("Name", m.getName()));
        m.setEmail(readValidOptional("E-mail", m.getEmail(), Validator::isValidEmail, EMAIL_RULE));
        m.setPhone(readValidOptional("Phone", m.getPhone(), Validator::isValidPhone, PHONE_RULE));
        System.out.println("Membership type:");
        m.setMembershipType(in.readEnum("Choose", MembershipType.class, m.getMembershipType()));

        service.updateMember(m);
        ConsoleUtil.success("Member #" + m.getId() + " updated successfully.");
    }

    private void deleteMember() throws LibraryException {
        Member m = service.getMember(in.readId("Enter ID of the member to delete: "));
        if (in.confirm("Delete " + m + "?")) {
            service.deleteMember(m.getId());
            ConsoleUtil.success("Member deleted.");
        } else {
            ConsoleUtil.info("Deletion cancelled.");
        }
    }

    private String readValidOptional(String label, String current, Predicate<String> rule, String message) {
        while (true) {
            String value = in.readOptional(label, current);
            if (rule.test(value)) {
                return value;
            }
            System.out.println("  [!] " + message);
        }
    }

    static void printMembers(List<Member> members) {
        if (members.isEmpty()) {
            ConsoleUtil.info("No members found.");
            return;
        }
        TablePrinter table = new TablePrinter("ID", "Name", "E-mail", "Phone", "Type", "Joined");
        for (Member m : members) {
            table.addRow(m.getId(), m.getName(), m.getEmail(), m.getPhone(), m.getMembershipType(), m.getJoinDate());
        }
        table.print();
    }
}
```

### Menu.java

```java
package com.library.ui;

import com.library.exception.DataAccessException;
import com.library.exception.LibraryException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Abstract, reusable menu screen (Template Method pattern).
 *
 * <p>The {@link #show()} loop - print options, read a choice, run it, repeat -
 * is written only once here. Subclasses just register their options with
 * {@link #option(String, Action)}.</p>
 */
public abstract class Menu {

    /** Something that happens when a menu entry is chosen. */
    @FunctionalInterface
    protected interface Action {
        void run() throws LibraryException;
    }

    /** {@code screen} is true for actions that print a result the user should read before the menu returns. */
    private record Entry(String label, Action action, boolean screen) {
    }

    protected final InputReader in;
    private final String title;
    private final String exitLabel;
    private final Map<Integer, Entry> entries = new LinkedHashMap<>();

    protected Menu(InputReader in, String title, String exitLabel) {
        this.in = in;
        this.title = title;
        this.exitLabel = exitLabel;
    }

    protected void option(String label, Action action) {
        entries.put(entries.size() + 1, new Entry(label, action, true));
    }

    /** Adds an entry that opens another menu (no heading or pause around it). */
    protected void submenu(String label, Menu menu) {
        entries.put(entries.size() + 1, new Entry(label, menu::show, false));
    }

    /** Displays the menu repeatedly until the user picks 0. */
    public void show() {
        boolean running = true;
        while (running) {
            printMenu();
            int choice = in.readInt("Enter your choice: ");
            if (choice == 0) {
                running = false;
            } else if (entries.containsKey(choice)) {
                Entry entry = entries.get(choice);
                if (entry.screen()) {
                    ConsoleUtil.heading(entry.label());
                }
                runSafely(entry.action());
                if (entry.screen()) {
                    in.pause();
                }
            } else {
                System.out.println("  [!] Invalid choice '" + choice + "'. Please select one of the listed options.");
            }
        }
    }

    private void printMenu() {
        System.out.println();
        ConsoleUtil.banner(title);
        entries.forEach((number, entry) -> System.out.printf("  %d. %s%n", number, entry.label()));
        System.out.printf("  0. %s%n", exitLabel);
        System.out.println(ConsoleUtil.LINE);
    }

    /** Runs an action and turns any expected error into a friendly message instead of a crash. */
    private static void runSafely(Action action) {
        try {
            action.run();
        } catch (DataAccessException e) {
            System.out.println("  [DATABASE ERROR] " + e.getMessage());
        } catch (LibraryException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
    }
}
```

## Package `com.library.util`

### TablePrinter.java

```java
package com.library.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Prints rows of data as a neatly aligned text table in the console.
 *
 * <pre>
 * TablePrinter table = new TablePrinter("ID", "Name");
 * table.addRow(1, "Alice");
 * table.print();
 * </pre>
 */
public class TablePrinter {

    private static final int MAX_CELL_WIDTH = 32;

    private final String[] headers;
    private final List<String[]> rows = new ArrayList<>();

    public TablePrinter(String... headers) {
        this.headers = headers;
    }

    public void addRow(Object... values) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            Object value = i < values.length ? values[i] : "";
            row[i] = truncate(value == null ? "-" : value.toString());
        }
        rows.add(row);
    }

    public void print() {
        int[] widths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            widths[i] = headers[i].length();
        }
        for (String[] row : rows) {
            for (int i = 0; i < row.length; i++) {
                widths[i] = Math.max(widths[i], row[i].length());
            }
        }

        String separator = buildSeparator(widths);
        System.out.println(separator);
        System.out.println(buildLine(headers, widths));
        System.out.println(separator);
        for (String[] row : rows) {
            System.out.println(buildLine(row, widths));
        }
        System.out.println(separator);
        System.out.println(rows.size() + " record(s) found.");
    }

    private static String buildSeparator(int[] widths) {
        StringBuilder sb = new StringBuilder("+");
        for (int width : widths) {
            sb.append("-".repeat(width + 2)).append('+');
        }
        return sb.toString();
    }

    private static String buildLine(String[] cells, int[] widths) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < widths.length; i++) {
            sb.append(' ').append(String.format("%-" + widths[i] + "s", cells[i])).append(" |");
        }
        return sb.toString();
    }

    private static String truncate(String text) {
        return text.length() <= MAX_CELL_WIDTH ? text : text.substring(0, MAX_CELL_WIDTH - 3) + "...";
    }
}
```

### Validator.java

```java
package com.library.util;

import com.library.exception.ValidationException;

import java.util.regex.Pattern;

/**
 * Central place for all input validation rules.
 * The class only contains static helpers, so it cannot be instantiated.
 */
public final class Validator {

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)*\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9]{7,15}$");
    private static final Pattern ISBN = Pattern.compile("^(\\d{9}[\\dX]|\\d{13})$");

    private Validator() {
    }

    public static void requireNonBlank(String value, String field) throws ValidationException {
        if (value == null || value.isBlank()) {
            throw new ValidationException(field + " cannot be empty.");
        }
    }

    public static void requireMaxLength(String value, int max, String field) throws ValidationException {
        if (value != null && value.length() > max) {
            throw new ValidationException(field + " cannot be longer than " + max + " characters.");
        }
    }

    public static void requireRange(int value, int min, int max, String field) throws ValidationException {
        if (value < min || value > max) {
            throw new ValidationException(field + " must be between " + min + " and " + max + ".");
        }
    }

    public static void requireEmail(String email) throws ValidationException {
        if (!isValidEmail(email)) {
            throw new ValidationException("Invalid e-mail address (example: name@example.com).");
        }
    }

    public static void requirePhone(String phone) throws ValidationException {
        if (!isValidPhone(phone)) {
            throw new ValidationException("Phone number must contain 7-15 digits, optionally starting with '+'.");
        }
    }

    /** Accepts ISBN-10 or ISBN-13; hyphens and spaces are ignored. */
    public static void requireIsbn(String isbn) throws ValidationException {
        if (!isValidIsbn(isbn)) {
            throw new ValidationException("ISBN must have 10 or 13 digits (hyphens are allowed).");
        }
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE.matcher(phone).matches();
    }

    public static boolean isValidIsbn(String isbn) {
        return isbn != null && ISBN.matcher(normalizeIsbn(isbn)).matches();
    }

    public static String normalizeIsbn(String isbn) {
        return isbn == null ? null : isbn.replaceAll("[\\s-]", "").toUpperCase();
    }
}
```

## Build file `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.library</groupId>
    <artifactId>library-management-system</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    <name>Library Management System (CLI)</name>

    <properties>
        <maven.compiler.release>17</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- JDBC driver for the embedded SQLite database -->
        <dependency>
            <groupId>org.xerial</groupId>
            <artifactId>sqlite-jdbc</artifactId>
            <version>3.46.1.3</version>
        </dependency>
        <!-- Silences the SLF4J "no provider" warning emitted by the driver -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-nop</artifactId>
            <version>2.0.16</version>
        </dependency>
    </dependencies>

    <build>
        <finalName>library-management-system</finalName>
        <plugins>
            <!-- Builds a single runnable JAR that already contains the JDBC driver -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.6.0</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals><goal>shade</goal></goals>
                        <configuration>
                            <createDependencyReducedPom>false</createDependencyReducedPom>
                            <transformers>
                                <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                    <mainClass>com.library.app.Main</mainClass>
                                </transformer>
                                <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
                            </transformers>
                            <filters>
                                <filter>
                                    <artifact>*:*</artifact>
                                    <excludes>
                                        <exclude>META-INF/*.SF</exclude>
                                        <exclude>META-INF/*.DSA</exclude>
                                        <exclude>META-INF/*.RSA</exclude>
                                        <exclude>META-INF/MANIFEST.MF</exclude>
                                    </excludes>
                                </filter>
                            </filters>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```
