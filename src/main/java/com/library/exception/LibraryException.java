package com.library.exception;

/**
 * Base class for all application-specific (checked) exceptions.
 * Having a common parent lets the UI layer catch every "expected" error in one place.
 */
public class LibraryException extends Exception {

    public LibraryException(String message) {
        super(message);
    }

    public LibraryException(String message, Throwable cause) {
        super(message, cause);
    }
}
