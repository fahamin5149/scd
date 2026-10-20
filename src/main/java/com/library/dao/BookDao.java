package com.library.dao;

import com.library.db.DatabaseManager;
import com.library.exception.LibraryException;
import com.library.exception.RecordNotFoundException;
import com.library.model.Book;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** Data access object for the {@code books} table. */
public class BookDao extends AbstractDao<Book> {

    public BookDao(DatabaseManager db) {
        super(db);
    }

    @Override
    public String entityName() {
        return "Book";
    }

    @Override
    protected String tableName() {
        return "books";
    }

    @Override
    protected Book mapRow(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getInt("id"));
        book.setIsbn(rs.getString("isbn"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setGenre(rs.getString("genre"));
        book.setPublicationYear(rs.getInt("publication_year"));
        book.setTotalCopies(rs.getInt("total_copies"));
        book.setAvailableCopies(rs.getInt("available_copies"));
        return book;
    }

    @Override
    public Book create(Book book) throws LibraryException {
        int id = executeInsert("""
                INSERT INTO books (isbn, title, author, genre, publication_year, total_copies, available_copies)
                VALUES (?, ?, ?, ?, ?, ?, ?)""",
                book.getIsbn(), book.getTitle(), book.getAuthor(), book.getGenre(),
                book.getPublicationYear(), book.getTotalCopies(), book.getAvailableCopies());
        book.setId(id);
        return book;
    }

    @Override
    public void update(Book book) throws LibraryException {
        int rows = executeUpdate("""
                UPDATE books SET isbn = ?, title = ?, author = ?, genre = ?,
                       publication_year = ?, total_copies = ?, available_copies = ?
                WHERE id = ?""",
                book.getIsbn(), book.getTitle(), book.getAuthor(), book.getGenre(),
                book.getPublicationYear(), book.getTotalCopies(), book.getAvailableCopies(), book.getId());
        if (rows == 0) {
            throw new RecordNotFoundException(entityName(), book.getId());
        }
    }

    public Optional<Book> findByIsbn(String isbn) throws LibraryException {
        List<Book> result = query(selectSql() + " WHERE isbn = ?", isbn);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    /** Case-insensitive search in title, author and genre. */
    public List<Book> search(String keyword) throws LibraryException {
        String pattern = "%" + keyword.toLowerCase() + "%";
        return query(selectSql()
                        + " WHERE lower(title) LIKE ? OR lower(author) LIKE ? OR lower(genre) LIKE ? ORDER BY title",
                pattern, pattern, pattern);
    }
}
