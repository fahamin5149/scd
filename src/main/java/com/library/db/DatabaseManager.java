package com.library.db;

import com.library.exception.DataAccessException;
import com.library.exception.LibraryException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Owns the single JDBC connection used by the application (Singleton pattern)
 * and creates the database schema the first time the program runs.
 *
 * <p>SQLite keeps all data in one file, so every record survives after the
 * program is closed and is loaded again on the next start.</p>
 */
public final class DatabaseManager {

    public static final String DEFAULT_DB_FILE = "library.db";

    private static DatabaseManager instance;

    private final String url;
    private Connection connection;

    private DatabaseManager(String dbFile) {
        this.url = "jdbc:sqlite:" + dbFile;
    }

    /** Returns the shared manager, creating it on first use. */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            String dbFile = System.getProperty("library.db", DEFAULT_DB_FILE);
            instance = new DatabaseManager(dbFile);
        }
        return instance;
    }

    /** Opens the connection (if needed) and makes sure all tables exist. */
    public void connect() throws DataAccessException {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url);
                try (Statement st = connection.createStatement()) {
                    // SQLite only enforces foreign keys when this pragma is switched on.
                    st.execute("PRAGMA foreign_keys = ON");
                }
                createSchema();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not connect to the database (" + url + "): " + e.getMessage(), e);
        }
    }

    public Connection getConnection() throws DataAccessException {
        connect();
        return connection;
    }

    /** A unit of work that must either fully succeed or be rolled back. */
    @FunctionalInterface
    public interface Transaction<R> {
        R execute() throws LibraryException;
    }

    /**
     * Runs several DAO calls as one atomic database transaction:
     * if any step throws, every change made by the earlier steps is undone.
     */
    public <R> R inTransaction(Transaction<R> work) throws LibraryException {
        Connection conn = getConnection();
        try {
            conn.setAutoCommit(false);
            R result = work.execute();
            conn.commit();
            return result;
        } catch (LibraryException | RuntimeException e) {
            rollback(conn);
            throw e;
        } catch (SQLException e) {
            rollback(conn);
            throw new DataAccessException("Transaction failed: " + e.getMessage(), e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {
                // connection is unusable; the next call to connect() will report it
            }
        }
    }

    private static void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            System.err.println("Warning: rollback failed: " + e.getMessage());
        }
    }

    public String getUrl() {
        return url;
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Warning: failed to close database connection: " + e.getMessage());
            }
        }
    }

    private void createSchema() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS books (
                        id               INTEGER PRIMARY KEY AUTOINCREMENT,
                        isbn             TEXT    NOT NULL UNIQUE,
                        title            TEXT    NOT NULL,
                        author           TEXT    NOT NULL,
                        genre            TEXT    NOT NULL,
                        publication_year INTEGER NOT NULL,
                        total_copies     INTEGER NOT NULL CHECK (total_copies > 0),
                        available_copies INTEGER NOT NULL CHECK (available_copies >= 0)
                    )""");
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS members (
                        id              INTEGER PRIMARY KEY AUTOINCREMENT,
                        name            TEXT NOT NULL,
                        email           TEXT NOT NULL UNIQUE COLLATE NOCASE,
                        phone           TEXT NOT NULL,
                        membership_type TEXT NOT NULL,
                        join_date       TEXT NOT NULL
                    )""");
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS loans (
                        id          INTEGER PRIMARY KEY AUTOINCREMENT,
                        book_id     INTEGER NOT NULL REFERENCES books(id),
                        member_id   INTEGER NOT NULL REFERENCES members(id),
                        issue_date  TEXT    NOT NULL,
                        due_date    TEXT    NOT NULL,
                        return_date TEXT
                    )""");
        }
    }
}
