package com.expense.core.database;

import java.sql.Connection;

/**
 * Supplies JDBC {@link Connection}s to repositories. Abstracted so the storage
 * backend (single connection, pool, Android SQLite bridge, future server
 * datasource) can vary without touching repository code.
 */
public interface ConnectionProvider {
    /** @return a usable connection. Ownership/closing semantics are defined by the implementation. */
    Connection connection();
}
