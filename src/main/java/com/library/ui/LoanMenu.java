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
