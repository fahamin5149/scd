package com.library.exception;

/** Thrown when a record with the requested ID does not exist in the database. */
public class RecordNotFoundException extends LibraryException {

    public RecordNotFoundException(String entityName, int id) {
        super(entityName + " with ID " + id + " was not found.");
    }
}
