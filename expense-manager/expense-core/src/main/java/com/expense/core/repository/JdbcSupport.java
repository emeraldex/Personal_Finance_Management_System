package com.expense.core.repository;

import com.expense.core.database.ConnectionProvider;
import com.expense.core.exception.PersistenceException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Small base class providing safe JDBC helpers shared by all repository
 * implementations: statement templating, generated-key extraction, and uniform
 * translation of {@link SQLException} into {@link PersistenceException}. This
 * removes duplicated boilerplate from concrete repositories.
 */
abstract class JdbcSupport {

    /** Functional mapping of a single {@link ResultSet} row to a domain object. */
    @FunctionalInterface
    protected interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    /** Binds parameters to a prepared statement. */
    @FunctionalInterface
    protected interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private final ConnectionProvider connections;

    protected JdbcSupport(ConnectionProvider connections) {
        this.connections = connections;
    }

    protected Connection conn() {
        return connections.connection();
    }

    /** Executes an INSERT and returns the auto-generated primary key. */
    protected long insert(String sql, Binder binder) {
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            binder.bind(ps);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
                throw new PersistenceException("No generated key returned for: " + sql, null);
            }
        } catch (SQLException e) {
            throw new PersistenceException("Insert failed: " + sql, e);
        }
    }

    /**
     * Executes an INSERT/UPSERT that ends in a {@code RETURNING <col>} clause and
     * returns the first column of the single returned row (typically the id).
     * Required because {@code RETURNING} produces a {@link ResultSet}, which
     * {@link java.sql.PreparedStatement#executeUpdate()} rejects.
     */
    protected long insertReturning(String sql, Binder binder) {
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new PersistenceException("No RETURNING row for: " + sql, null);
            }
        } catch (SQLException e) {
            throw new PersistenceException("Upsert failed: " + sql, e);
        }
    }

    /** Executes an UPDATE/DELETE and returns the affected row count. */
    protected int execute(String sql, Binder binder) {
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            binder.bind(ps);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("Update failed: " + sql, e);
        }
    }

    /** Executes a query and maps every row. */
    protected <T> List<T> query(String sql, Binder binder, RowMapper<T> mapper) {
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(mapper.map(rs));
                }
                return out;
            }
        } catch (SQLException e) {
            throw new PersistenceException("Query failed: " + sql, e);
        }
    }

    /** Executes a query expected to yield at most one row. */
    protected <T> Optional<T> queryOne(String sql, Binder binder, RowMapper<T> mapper) {
        List<T> rows = query(sql, binder, mapper);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    /** No-op binder for parameterless statements. */
    protected static final Binder NO_ARGS = ps -> { };
}
