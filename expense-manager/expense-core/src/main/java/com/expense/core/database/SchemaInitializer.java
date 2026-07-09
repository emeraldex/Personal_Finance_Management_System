package com.expense.core.database;

import com.expense.core.exception.PersistenceException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Loads and executes the bundled {@code db/schema.sql} script. Statements are
 * split on semicolons; the script is written to be idempotent
 * ({@code CREATE TABLE IF NOT EXISTS}) so it can run on every startup.
 */
public final class SchemaInitializer {

    private static final String SCHEMA_RESOURCE = "/db/schema.sql";

    private SchemaInitializer() {
    }

    /** Executes the schema script against {@code connection}. */
    public static void initialize(Connection connection) {
        String script = stripLineComments(loadScript());
        try (Statement st = connection.createStatement()) {
            for (String stmt : script.split(";")) {
                String trimmed = stmt.strip();
                if (!trimmed.isEmpty()) {
                    st.execute(trimmed);
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("Failed to initialise schema", e);
        }
    }

    /**
     * Removes {@code --} line comments so that semicolons appearing inside
     * comments do not corrupt the statement split. The schema contains no
     * string literals with {@code --}, so this line-based approach is safe here.
     */
    private static String stripLineComments(String script) {
        StringBuilder sb = new StringBuilder(script.length());
        for (String line : script.split("\n")) {
            int comment = line.indexOf("--");
            sb.append(comment >= 0 ? line.substring(0, comment) : line).append('\n');
        }
        return sb.toString();
    }

    private static String loadScript() {
        try (InputStream in = SchemaInitializer.class.getResourceAsStream(SCHEMA_RESOURCE)) {
            if (in == null) {
                throw new PersistenceException("Schema resource not found: " + SCHEMA_RESOURCE, null);
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            throw new PersistenceException("Failed to read schema resource", e);
        }
    }
}
