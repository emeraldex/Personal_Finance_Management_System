package com.expense.android.data;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.expense.core.exception.PersistenceException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Base class providing the small JDBC-free SQL helpers shared by the Android
 * repository adapters. Mirrors the core's {@code JdbcSupport} but runs against
 * {@link SQLiteDatabase} so the app needs no JDBC driver on-device. Every SQLite
 * failure is translated to the core {@link PersistenceException} so the services
 * above behave identically to the desktop (JDBC) build.
 */
abstract class AndroidSqlSupport {

    /** Maps a single {@link Cursor} row to a domain object. */
    protected interface RowMapper<T> {
        T map(Cursor cursor);
    }

    protected final SQLiteDatabase db;

    protected AndroidSqlSupport(AndroidDatabase database) {
        this.db = database.db();
    }

    /** Executes an INSERT and returns the generated row id. */
    protected long insert(String sql, Object... args) {
        SQLiteStatement st = db.compileStatement(sql);
        try {
            bindAll(st, args);
            return st.executeInsert();
        } catch (SQLException e) {
            throw new PersistenceException("Insert failed: " + sql, e);
        } finally {
            st.close();
        }
    }

    /** Executes an UPDATE/DELETE and returns the affected row count. */
    protected int execute(String sql, Object... args) {
        SQLiteStatement st = db.compileStatement(sql);
        try {
            bindAll(st, args);
            return st.executeUpdateDelete();
        } catch (SQLException e) {
            throw new PersistenceException("Update failed: " + sql, e);
        } finally {
            st.close();
        }
    }

    /** Runs a query and maps every row. {@code args} may be {@code null} for none. */
    protected <T> List<T> query(String sql, String[] args, RowMapper<T> mapper) {
        try (Cursor cursor = db.rawQuery(sql, args)) {
            List<T> out = new ArrayList<>();
            while (cursor.moveToNext()) {
                out.add(mapper.map(cursor));
            }
            return out;
        } catch (SQLException e) {
            throw new PersistenceException("Query failed: " + sql, e);
        }
    }

    /** Runs a query expected to yield at most one row. */
    protected <T> Optional<T> queryOne(String sql, String[] args, RowMapper<T> mapper) {
        List<T> rows = query(sql, args, mapper);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    /** Builds a {@code String[]} of selection args (SQLite binds query args as text). */
    protected static String[] args(Object... values) {
        String[] out = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            out[i] = String.valueOf(values[i]);
        }
        return out;
    }

    private static void bindAll(SQLiteStatement st, Object[] args) {
        for (int i = 0; i < args.length; i++) {
            Object a = args[i];
            int idx = i + 1;
            if (a == null) {
                st.bindNull(idx);
            } else if (a instanceof Long) {
                st.bindLong(idx, (Long) a);
            } else if (a instanceof Integer) {
                st.bindLong(idx, ((Integer) a).longValue());
            } else if (a instanceof Boolean) {
                st.bindLong(idx, ((Boolean) a) ? 1 : 0);
            } else {
                st.bindString(idx, a.toString());
            }
        }
    }
}
