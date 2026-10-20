package com.library.exception;

/** Thrown when inserting/updating a record would violate a uniqueness rule (e.g. same ISBN or e-mail). */
public class DuplicateRecordException extends LibraryException {

    public DuplicateRecordException(String message) {
        super(message);
    }
}
