package com.expense.desktop.viewmodel;

import com.expense.core.report.ImportResult;
import com.expense.core.service.ExpenseManager;
import com.expense.desktop.Settings;
import com.expense.desktop.io.PoiWorkbookImporter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Currency;
import java.util.Objects;

/**
 * ViewModel for the Settings / Data tab. Surfaces the persisted auto-categorise
 * preference, backs up the SQLite database to a chosen file, and imports a
 * transaction workbook via {@link PoiWorkbookImporter}. After an import it runs
 * {@code onDataChanged} so every read screen refreshes.
 */
public final class SettingsViewModel {

    private final Settings settings;
    private final ExpenseManager manager;
    private final Currency currency;
    private final Path dbPath;
    private final Runnable onDataChanged;

    private final BooleanProperty autoCategorize = new SimpleBooleanProperty();
    private final StringProperty status = new SimpleStringProperty("");

    public SettingsViewModel(Settings settings, ExpenseManager manager, Currency currency,
                             Path dbPath, Runnable onDataChanged) {
        this.settings = Objects.requireNonNull(settings);
        this.manager = Objects.requireNonNull(manager);
        this.currency = Objects.requireNonNull(currency);
        this.dbPath = Objects.requireNonNull(dbPath);
        this.onDataChanged = onDataChanged == null ? () -> { } : onDataChanged;
        autoCategorize.set(settings.isAutoCategorize());
        autoCategorize.addListener((obs, was, now) -> settings.setAutoCategorize(now));
    }

    /** Copies the live database file to {@code target} as a backup snapshot. */
    public boolean backupDatabase(File target) {
        try {
            Files.copy(dbPath, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            status.set("Backup saved to " + target.getName());
            return true;
        } catch (IOException e) {
            status.set("Backup failed: " + e.getMessage());
            return false;
        }
    }

    /** Imports transactions from an Excel workbook and reports the outcome. */
    public boolean importExcel(File file) {
        try (InputStream in = new BufferedInputStream(Files.newInputStream(file.toPath()))) {
            ImportResult result = new PoiWorkbookImporter(manager, currency).importWorkbook(in);
            StringBuilder msg = new StringBuilder("Imported ")
                    .append(result.imported()).append(", skipped ").append(result.skipped());
            if (!result.warnings().isEmpty()) {
                msg.append(" — ").append(result.warnings().size()).append(" note(s): ")
                        .append(String.join("; ", result.warnings()));
            }
            status.set(msg.toString());
            onDataChanged.run();
            return true;
        } catch (IOException e) {
            status.set("Import failed: " + e.getMessage());
            return false;
        } catch (RuntimeException e) {
            status.set("Import failed: " + e.getMessage());
            return false;
        }
    }

    public String getDatabasePath() {
        return dbPath.toString();
    }

    public String getCurrencyCode() {
        return currency.getCurrencyCode();
    }

    public BooleanProperty autoCategorizeProperty() { return autoCategorize; }
    public StringProperty statusProperty() { return status; }
}
