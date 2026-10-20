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
