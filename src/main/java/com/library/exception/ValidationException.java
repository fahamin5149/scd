package com.library.exception;

/** Thrown when a value entered by the user breaks a business or format rule. */
public class ValidationException extends LibraryException {

    public ValidationException(String message) {
        super(message);
    }
}
