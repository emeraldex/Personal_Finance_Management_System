package com.expense.desktop;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

/**
 * Simple, file-backed user preferences stored alongside the database in
 * {@code ~/.expense-manager/settings.properties}. Kept free of any JavaFX
 * dependency so it can be unit-tested headlessly. Writes are best-effort: a
 * failure to persist never breaks the app.
 */
public final class Settings {

    private static final String KEY_AUTO_CATEGORIZE = "autoCategorize";

    private final Path file;
    private boolean autoCategorize = true;

    public Settings(Path dataDir) {
        this.file = Objects.requireNonNull(dataDir).resolve("settings.properties");
        load();
    }

    public boolean isAutoCategorize() {
        return autoCategorize;
    }

    public void setAutoCategorize(boolean value) {
        this.autoCategorize = value;
        save();
    }

    private void load() {
        if (!Files.exists(file)) {
            return;
        }
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            p.load(in);
        } catch (IOException e) {
            return;
        }
        autoCategorize = Boolean.parseBoolean(p.getProperty(KEY_AUTO_CATEGORIZE, "true"));
    }

    private void save() {
        Properties p = new Properties();
        p.setProperty(KEY_AUTO_CATEGORIZE, String.valueOf(autoCategorize));
        try {
            Files.createDirectories(file.getParent());
            try (OutputStream out = Files.newOutputStream(file)) {
                p.store(out, "Expense Manager settings");
            }
        } catch (IOException ignored) {
            // Best-effort persistence; the in-memory value still applies this session.
        }
    }
}
