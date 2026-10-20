package com.library.model;

import com.library.exception.ValidationException;

/**
 * Abstract root of every record stored in the database.
 * It holds the primary key and forces each subclass to provide its own validation rules.
 */
public abstract class Entity {

    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * Checks that the object is in a valid state before it is written to the database.
     *
     * @throws ValidationException if any field is invalid
     */
    public abstract void validate() throws ValidationException;
}
