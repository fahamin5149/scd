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
