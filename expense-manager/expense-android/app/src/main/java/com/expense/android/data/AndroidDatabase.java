package com.expense.android.data;

import android.database.sqlite.SQLiteDatabase;

import com.expense.core.exception.PersistenceException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * On-device SQLite database backing the Android repository adapters. Opens (or
 * creates) the database file, enables foreign-key enforcement, and applies the
 * shared {@code db/schema.sql} — the very same script the desktop build uses,
 * loaded from the {@code expense-core} jar on the classpath — so the schema is
 * defined in exactly one place.
 *
 * <p>{@link AutoCloseable} so it can be handed to {@code ExpenseManager} as the
 * resource released when the manager closes.</p>
 */
public final class AndroidDatabase implements AutoCloseable {

    private static final String SCHEMA_RESOURCE = "/db/schema.sql";

    private final SQLiteDatabase db;

    private AndroidDatabase(SQLiteDatabase db) {
        this.db = db;
    }

    /** Opens (creating if needed) the database at {@code path} and applies the schema. */
    public static AndroidDatabase open(String path) {
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(path, null);
        db.setForeignKeyConstraintsEnabled(true);
        applySchema(db);
        return new AndroidDatabase(db);
    }

    SQLiteDatabase db() {
        return db;
    }

    @Override
    public void close() {
        db.close();
    }

    private static void applySchema(SQLiteDatabase db) {
        String script = stripLineComments(loadScript());
        for (String statement : script.split(";")) {
            String trimmed = statement.trim();
            if (!trimmed.isEmpty()) {
                db.execSQL(trimmed);
            }
        }
    }

    private static String stripLineComments(String script) {
        StringBuilder sb = new StringBuilder(script.length());
        for (String line : script.split("\n")) {
            int comment = line.indexOf("--");
            sb.append(comment >= 0 ? line.substring(0, comment) : line).append('\n');
        }
        return sb.toString();
    }

    private static String loadScript() {
        try (InputStream in = AndroidDatabase.class.getResourceAsStream(SCHEMA_RESOURCE)) {
            if (in == null) {
                throw new PersistenceException("Schema resource not found: " + SCHEMA_RESOURCE, null);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            throw new PersistenceException("Failed to read schema resource", e);
        }
    }
}
