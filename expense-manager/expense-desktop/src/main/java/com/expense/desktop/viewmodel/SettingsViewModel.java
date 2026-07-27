package com.expense.desktop.viewmodel;

import com.expense.core.domain.Account;
import com.expense.core.network.BankFeedClient;
import com.expense.core.network.BankFeedEntry;
import com.expense.core.report.ImportResult;
import com.expense.core.service.BudgetAlertService;
import com.expense.core.service.ExpenseManager;
import com.expense.desktop.Settings;
import com.expense.desktop.io.PoiWorkbookImporter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * ViewModel for the Settings / Data tab. Surfaces the persisted entry
 * preferences (auto-categorise, budget alerts), manages the fixed exchange-rate
 * table (persisted in Settings and mirrored into the core's live provider),
 * backs up the SQLite database to a chosen file, and imports a transaction
 * workbook via {@link PoiWorkbookImporter}. After a data-changing action it runs
 * {@code onDataChanged} so every read screen refreshes.
 */
public final class SettingsViewModel {

    private final Settings settings;
    private final ExpenseManager manager;
    private final Currency currency;
    private final Path dbPath;
    private final Runnable onDataChanged;
    private final BankFeedClient bankFeed;
    private final BudgetAlertService budgetAlerts;

    private final BooleanProperty autoCategorize = new SimpleBooleanProperty();
    private final BooleanProperty budgetAlerts = new SimpleBooleanProperty();
    private final StringProperty fxCode = new SimpleStringProperty("");
    private final StringProperty fxRate = new SimpleStringProperty("");
    private final StringProperty fxRatesDisplay = new SimpleStringProperty("");
    private final ObjectProperty<Account> bankAccount = new SimpleObjectProperty<>();
    private final ObservableList<Account> accounts = FXCollections.observableArrayList();
    private final StringProperty status = new SimpleStringProperty("");

    /**
     * @param bankFeed     the bank-feed seam implementation used for statement import
     * @param budgetAlerts re-checks budgets affected by an import; {@code null} disables alerts
     */
    public SettingsViewModel(Settings settings, ExpenseManager manager, Currency currency,
                             Path dbPath, Runnable onDataChanged, BankFeedClient bankFeed,
                             BudgetAlertService budgetAlerts) {
        this.settings = Objects.requireNonNull(settings);
        this.manager = Objects.requireNonNull(manager);
        this.currency = Objects.requireNonNull(currency);
        this.dbPath = Objects.requireNonNull(dbPath);
        this.onDataChanged = onDataChanged == null ? () -> { } : onDataChanged;
        this.bankFeed = Objects.requireNonNull(bankFeed);
        this.budgetAlerts = budgetAlerts;
        reloadAccounts();
        autoCategorize.set(settings.isAutoCategorize());
        autoCategorize.addListener((obs, was, now) -> settings.setAutoCategorize(now));
        budgetAlerts.set(settings.isBudgetAlerts());
        budgetAlerts.addListener((obs, was, now) -> settings.setBudgetAlerts(now));
        refreshFxDisplay();
    }

    /**
     * Sets (or replaces) a fixed exchange rate from the {@code fxCode}/{@code fxRate}
     * fields: 1 unit of the entered currency = rate units of the app currency.
     * Persists to Settings and updates the core's live rate table.
     */
    public boolean setFxRate() {
        String code = fxCode.get().trim().toUpperCase(Locale.ROOT);
        try {
            Currency entered = Currency.getInstance(code);
            if (entered.equals(currency)) {
                status.set("Rates are relative to " + currency.getCurrencyCode()
                        + " — enter a foreign currency");
                return false;
            }
            BigDecimal rate = new BigDecimal(fxRate.get().trim());
            if (rate.signum() <= 0) {
                status.set("Rate must be positive");
                return false;
            }
            settings.putFxRate(code, rate);
            manager.exchangeRates().setRate(entered, currency, rate);
            refreshFxDisplay();
            status.set("Rate saved: 1 " + code + " = " + rate.toPlainString() + " "
                    + currency.getCurrencyCode());
            onDataChanged.run();
            return true;
        } catch (IllegalArgumentException e) {
            // Covers both an unknown ISO code and a non-numeric rate.
            status.set("Enter a valid ISO currency code (e.g. USD) and a numeric rate");
            return false;
        }
    }

    /** Removes the fixed rate for the currency in the {@code fxCode} field. */
    public boolean removeFxRate() {
        String code = fxCode.get().trim().toUpperCase(Locale.ROOT);
        try {
            Currency entered = Currency.getInstance(code);
            settings.removeFxRate(code);
            manager.exchangeRates().removeRate(entered, currency);
            refreshFxDisplay();
            status.set("Rate removed for " + code);
            onDataChanged.run();
            return true;
        } catch (IllegalArgumentException e) {
            status.set("Enter a valid ISO currency code (e.g. USD)");
            return false;
        }
    }

    private void refreshFxDisplay() {
        var rates = settings.fxRates();
        fxRatesDisplay.set(rates.isEmpty()
                ? "No rates yet"
                : rates.entrySet().stream()
                        .map(e -> "1 " + e.getKey() + " = " + e.getValue().toPlainString() + " "
                                + currency.getCurrencyCode())
                        .collect(Collectors.joining("\n")));
    }

    /** Reloads the account picker for statement import, keeping a still-valid selection. */
    public void reloadAccounts() {
        accounts.setAll(manager.accounts().list().stream().filter(a -> !a.archived()).toList());
        if (bankAccount.get() == null || !accounts.contains(bankAccount.get())) {
            bankAccount.set(accounts.isEmpty() ? null : accounts.get(0));
        }
    }

    /**
     * Imports a bank-statement CSV into the selected account through the
     * bank-feed seam: parses drafts, then routes/de-dupes/categorises/converts
     * via the core {@code BankFeedImportService}.
     */
    public boolean importBankStatement(File file) {
        Account target = bankAccount.get();
        if (target == null) {
            status.set("Choose an account for the statement");
            return false;
        }
        try {
            List<BankFeedEntry> entries = bankFeed.fetch(file.getAbsolutePath(),
                    LocalDate.of(1970, 1, 1), LocalDate.now());
            ImportResult result = manager.bankFeedImports().importInto(target.id(), entries, budgetAlerts);
            StringBuilder msg = new StringBuilder("Bank feed: imported ")
                    .append(result.imported()).append(", skipped ").append(result.skipped());
            if (!result.warnings().isEmpty()) {
                msg.append(" — ").append(result.warnings().size()).append(" note(s): ")
                        .append(String.join("; ", result.warnings()));
            }
            status.set(msg.toString());
            onDataChanged.run();
            return true;
        } catch (RuntimeException e) {
            status.set("Bank import failed: " + e.getMessage());
            return false;
        }
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
    public BooleanProperty budgetAlertsProperty() { return budgetAlerts; }
    public StringProperty fxCodeProperty() { return fxCode; }
    public StringProperty fxRateProperty() { return fxRate; }
    public StringProperty fxRatesDisplayProperty() { return fxRatesDisplay; }
    public ObjectProperty<Account> bankAccountProperty() { return bankAccount; }
    public ObservableList<Account> getAccounts() { return accounts; }
    public StringProperty statusProperty() { return status; }
}
