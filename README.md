# Library Management System (Java CLI + SQLite)

A menu-driven, command-line Library Management System written in Java.
Librarians can manage **books**, **members** and **loans** (issue / return)
from the Command Prompt or a terminal. All records are stored in an SQLite
database file (`library.db`) through JDBC, so they survive program restarts.

The full project report (introduction, problem statement, objectives, tools,
source code, output screenshots and conclusion) is in
[`docs/Project_Report.docx`](docs/Project_Report.docx) and
[`docs/REPORT.md`](docs/REPORT.md).

## Features

| Area | Operations |
|------|------------|
| Books | Add, view all, search (title/author/genre), view details, update, delete |
| Members | Register, view all, search by name, view details, update, delete |
| Loans | Issue, return (with late fine), view all / active / overdue, member history, change due date, delete record |
| Report | Library summary (titles, copies, members, loans, overdue, fines) |

Validation & error handling: invalid menu choices, non-numeric input, bad
ISBN / e-mail / phone / year / date formats, duplicate ISBNs and e-mails,
unknown IDs, borrowing limits, unavailable copies, double returns, deleting
records that are still in use, and database connection failures.

## Requirements

* Java 17 or newer (JDK)
* Apache Maven 3.8+ (downloads the SQLite JDBC driver automatically)

No database server needs to be installed: SQLite is embedded and the
database file is created automatically on first run.

## Build & run

```sh
mvn package
java -jar target/library-management-system.jar
```

or simply run `run.bat` (Windows) / `./run.sh` (Linux/macOS).

The database is created as `library.db` in the current folder. Use
`-Dlibrary.db=<path>` to store it elsewhere:

```sh
java -Dlibrary.db=C:\data\library.db -jar target\library-management-system.jar
```

### Running without Maven

Download `sqlite-jdbc-3.46.1.3.jar`, `slf4j-api-2.0.16.jar` and
`slf4j-nop-2.0.16.jar` from Maven Central into a `lib` folder, then:

```sh
javac -d out -cp "lib/*" $(find src/main/java -name "*.java")
java -cp "out:lib/*" com.library.app.Main        # Linux/macOS
java -cp "out;lib/*" com.library.app.Main        # Windows
```

## Project structure

```
src/main/java/com/library/
├── app/        Main.java – entry point, wires all layers together
├── db/         DatabaseManager – JDBC connection (singleton), schema, transactions
├── model/      Entity, Person, Member, Book, Loan, MembershipType – domain classes
├── dao/        CrudDao, AbstractDao, BookDao, MemberDao, LoanDao – SQL / CRUD
├── service/    BookService, MemberService, LoanService – business rules
├── ui/         Menu, MainMenu, BookMenu, MemberMenu, LoanMenu, InputReader, ConsoleUtil
├── util/       Validator, TablePrinter
└── exception/  LibraryException and its subclasses
```

## Database schema

```
books   (id PK, isbn UNIQUE, title, author, genre, publication_year, total_copies, available_copies)
members (id PK, name, email UNIQUE, phone, membership_type, join_date)
loans   (id PK, book_id FK→books, member_id FK→members, issue_date, due_date, return_date)
```

| Membership | Max books | Loan period |
|------------|-----------|-------------|
| STUDENT    | 3         | 14 days     |
| FACULTY    | 10        | 30 days     |
| PUBLIC     | 2         | 7 days      |

Late returns are fined Rs. 10 per day.
