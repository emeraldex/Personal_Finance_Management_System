package com.expense.desktop;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Simple, file-backed user preferences stored alongside the database in
 * {@code ~/.expense-manager/settings.properties}. Kept free of any JavaFX
 * dependency so it can be unit-tested headlessly. Writes are best-effort: a
 * failure to persist never breaks the app.
 */
public final class Settings {

    private static final String KEY_AUTO_CATEGORIZE = "autoCategorize";
    private static final String KEY_BUDGET_ALERTS = "budgetAlerts";
    private static final String KEY_FX_PREFIX = "fxRate.";

    private final Path file;
    private boolean autoCategorize = true;
    private boolean budgetAlerts = true;
    /** ISO code -> units of the app currency per one unit of that currency. */
    private final Map<String, BigDecimal> fxRates = new TreeMap<>();

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

    public boolean isBudgetAlerts() {
        return budgetAlerts;
    }

    public void setBudgetAlerts(boolean value) {
        this.budgetAlerts = value;
        save();
    }

    /** Fixed exchange rates, keyed by ISO code, sorted for stable display. */
    public Map<String, BigDecimal> fxRates() {
        return Map.copyOf(fxRates);
    }

    public void putFxRate(String currencyCode, BigDecimal ratePerUnit) {
        fxRates.put(currencyCode, ratePerUnit);
        save();
    }

    public void removeFxRate(String currencyCode) {
        fxRates.remove(currencyCode);
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
        budgetAlerts = Boolean.parseBoolean(p.getProperty(KEY_BUDGET_ALERTS, "true"));
        for (String key : p.stringPropertyNames()) {
            if (key.startsWith(KEY_FX_PREFIX)) {
                try {
                    fxRates.put(key.substring(KEY_FX_PREFIX.length()),
                            new BigDecimal(p.getProperty(key).trim()));
                } catch (NumberFormatException ignored) {
                    // A hand-edited bad rate is dropped rather than breaking startup.
                }
            }
        }
    }

    private void save() {
        Properties p = new Properties();
        p.setProperty(KEY_AUTO_CATEGORIZE, String.valueOf(autoCategorize));
        p.setProperty(KEY_BUDGET_ALERTS, String.valueOf(budgetAlerts));
        fxRates.forEach((code, rate) -> p.setProperty(KEY_FX_PREFIX + code, rate.toPlainString()));
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
