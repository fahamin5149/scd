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
