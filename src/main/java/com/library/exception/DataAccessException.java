package com.library.exception;

/**
 * Wraps low-level {@link java.sql.SQLException}s so that the upper layers
 * never depend on JDBC classes directly (abstraction of the persistence layer).
 */
public class DataAccessException extends LibraryException {

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
