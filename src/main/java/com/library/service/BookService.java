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
