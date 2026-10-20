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
