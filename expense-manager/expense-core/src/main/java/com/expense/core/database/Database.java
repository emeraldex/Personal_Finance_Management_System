package com.expense.core.database;

import com.expense.core.exception.PersistenceException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Owns a single SQLite {@link Connection} for the lifetime of the application.
 *
 * <p>A single serialized connection is appropriate for a single-user desktop or
 * mobile app and sidesteps SQLite's per-connection in-memory database semantics
 * (each {@code :memory:} connection would otherwise be a separate database).
 * Foreign-key enforcement is enabled on the connection, and the schema is
 * initialised on construction.</p>
 *
 * <p>The class is {@link AutoCloseable}; closing releases the JDBC connection.</p>
 */
public final class Database implements ConnectionProvider, AutoCloseable {

    private final Connection connection;

    private Database(Connection connection) {
        this.connection = connection;
    }

    /** Opens an on-disk database at {@code jdbcPath} (e.g. {@code /home/me/expenses.db}). */
    public static Database openFile(String filePath) {
        return open("jdbc:sqlite:" + filePath);
    }

    /** Opens a private in-memory database; primarily used by tests. */
    public static Database openInMemory() {
        return open("jdbc:sqlite::memory:");
    }

    private static Database open(String url) {
        try {
            Connection conn = DriverManager.getConnection(url);
            try (Statement st = conn.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            }
            SchemaInitializer.initialize(conn);
            return new Database(conn);
        } catch (SQLException e) {
            throw new PersistenceException("Failed to open database: " + url, e);
        }
    }

    @Override
    public Connection connection() {
        return connection;
    }

    @Override
    public void close() {
        try {
            if (!connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new PersistenceException("Failed to close database", e);
        }
    }
}
