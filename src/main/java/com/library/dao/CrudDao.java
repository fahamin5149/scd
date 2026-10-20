package com.library.dao;

import com.library.exception.LibraryException;
import com.library.exception.RecordNotFoundException;
import com.library.model.Entity;

import java.util.List;
import java.util.Optional;

/**
 * Generic contract for Create, Read, Update and Delete operations.
 * Each concrete DAO hides the SQL needed for one table behind these methods.
 *
 * @param <T> the entity type handled by the DAO
 */
public interface CrudDao<T extends Entity> {

    /** Inserts a new record and returns it with its generated ID. */
    T create(T entity) throws LibraryException;

    Optional<T> findById(int id) throws LibraryException;

    List<T> findAll() throws LibraryException;

    /** Saves changes to an existing record. */
    void update(T entity) throws LibraryException;

    /** Deletes the record with the given ID. */
    void delete(int id) throws LibraryException;

    /** Convenience method: like {@link #findById(int)} but fails if nothing is found. */
    default T getById(int id) throws LibraryException {
        return findById(id).orElseThrow(() -> new RecordNotFoundException(entityName(), id));
    }

    /** Name used in user-facing messages, e.g. "Book". */
    String entityName();
}
