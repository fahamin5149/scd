package com.library.dao;

import com.library.db.DatabaseManager;
import com.library.exception.DataAccessException;
import com.library.exception.LibraryException;
import com.library.exception.RecordNotFoundException;
import com.library.model.Entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Shares the JDBC plumbing that is identical for every table
 * (finding by ID, listing, deleting, mapping result sets) so that the
 * concrete DAOs only describe what is specific to their entity.
 */
public abstract class AbstractDao<T extends Entity> implements CrudDao<T> {

    protected final DatabaseManager db;

    protected AbstractDao(DatabaseManager db) {
        this.db = db;
    }

    /** Name of the database table. */
    protected abstract String tableName();

    /** Converts the current row of a result set into an entity object. */
    protected abstract T mapRow(ResultSet rs) throws SQLException;

    /** SELECT used for reading records; subclasses may override it to add joins. */
    protected String selectSql() {
        return "SELECT * FROM " + tableName();
    }

    /** Column used for ordering lists. */
    protected String idColumn() {
        return "id";
    }

    protected Connection connection() throws DataAccessException {
        return db.getConnection();
    }

    @Override
    public Optional<T> findById(int id) throws LibraryException {
        List<T> result = query(selectSql() + " WHERE " + idColumn() + " = ?", id);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public List<T> findAll() throws LibraryException {
        return query(selectSql() + " ORDER BY " + idColumn());
    }

    @Override
    public void delete(int id) throws LibraryException {
        int rows = executeUpdate("DELETE FROM " + tableName() + " WHERE id = ?", id);
        if (rows == 0) {
            throw new RecordNotFoundException(entityName(), id);
        }
    }

    /** Runs a SELECT with positional parameters and maps every row. */
    protected List<T> query(String sql, Object... params) throws DataAccessException {
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to read " + entityName().toLowerCase() + " records: " + e.getMessage(), e);
        }
    }

    /** Runs a SELECT that returns a single number, such as COUNT(*). */
    protected int queryInt(String sql, Object... params) throws DataAccessException {
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database query failed: " + e.getMessage(), e);
        }
    }

    /** Runs INSERT/UPDATE/DELETE and returns the number of affected rows. */
    protected int executeUpdate(String sql, Object... params) throws DataAccessException {
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save " + entityName().toLowerCase() + ": " + e.getMessage(), e);
        }
    }

    /** Runs an INSERT and returns the auto-generated primary key. */
    protected int executeInsert(String sql, Object... params) throws DataAccessException {
        try (PreparedStatement ps = connection().prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            bind(ps, params);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new DataAccessException("No ID was generated for the new " + entityName().toLowerCase() + ".", null);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to add " + entityName().toLowerCase() + ": " + e.getMessage(), e);
        }
    }

    private static void bind(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }
}
