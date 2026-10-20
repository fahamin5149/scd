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
