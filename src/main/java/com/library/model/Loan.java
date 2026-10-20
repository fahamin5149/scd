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
