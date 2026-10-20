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
