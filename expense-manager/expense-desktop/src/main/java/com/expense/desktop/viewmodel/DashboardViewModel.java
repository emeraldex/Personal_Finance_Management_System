package com.expense.desktop.viewmodel;

import com.expense.core.domain.Transaction;
import com.expense.core.report.CategoryBreakdownItem;
import com.expense.core.report.MonthlySummary;
import com.expense.core.report.MonthlySummaryCsvExporter;
import com.expense.core.report.PdfSummaryExporter;
import com.expense.core.report.PoiWorkbookExporter;
import com.expense.core.report.TransactionCsvExporter;
import com.expense.core.service.ExpenseManager;
import com.expense.desktop.io.TransactionWorkbookExporter;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ViewModel for the dashboard. Exposes JavaFX observable properties the View
 * binds to; it never references any JavaFX node, keeping it headless-testable.
 * All data comes from the injected {@link ExpenseManager}.
 *
 * <p>Holds the currently viewed month so the View can page through history, and
 * offers CSV export of the current month's summary and transactions.</p>
 */
public final class DashboardViewModel {

    private final ExpenseManager manager;

    private YearMonth currentMonth = YearMonth.now();

    private final StringProperty month = new SimpleStringProperty();
    private final StringProperty totalIncome = new SimpleStringProperty("-");
    private final StringProperty totalExpense = new SimpleStringProperty("-");
    private final StringProperty netBalance = new SimpleStringProperty("-");
    private final StringProperty savings = new SimpleStringProperty("-");
    private final StringProperty outstanding = new SimpleStringProperty("-");
    private final StringProperty status = new SimpleStringProperty("");
    private final ObservableList<CategoryBreakdownItem> categoryBreakdown =
            FXCollections.observableArrayList();

    public DashboardViewModel(ExpenseManager manager) {
        this.manager = Objects.requireNonNull(manager);
    }

    /** Loads the summary for the current calendar month. */
    public void loadCurrentMonth() {
        currentMonth = YearMonth.now();
        load(currentMonth);
    }

    /** Reloads whatever month is currently selected (after data changes elsewhere). */
    public void reload() {
        load(currentMonth);
    }

    /** Moves to the next month and reloads. */
    public void nextMonth() {
        currentMonth = currentMonth.plusMonths(1);
        load(currentMonth);
    }

    /** Moves to the previous month and reloads. */
    public void prevMonth() {
        currentMonth = currentMonth.minusMonths(1);
        load(currentMonth);
    }

    /** Loads and publishes the summary for {@code ym}. */
    public void load(YearMonth ym) {
        currentMonth = ym;
        MonthlySummary s = manager.summaries().summarize(ym);
        month.set(ym.toString());
        totalIncome.set(s.totalIncome().toString());
        totalExpense.set(s.totalExpense().toString());
        netBalance.set(s.netBalance().toString());
        savings.set(s.savings().toString());
        outstanding.set(s.outstanding().toString());
        categoryBreakdown.setAll(s.categoryBreakdown());
    }

    /** Writes the current month's summary to {@code file} as CSV. */
    public boolean exportSummaryCsv(File file) {
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
            new MonthlySummaryCsvExporter().export(manager.summaries().summarize(currentMonth), out);
            status.set("Summary exported to " + file.getName());
            return true;
        } catch (IOException e) {
            status.set("Export failed: " + e.getMessage());
            return false;
        }
    }

    /** Writes the current month's transactions (expenses + income) to {@code file} as CSV. */
    public boolean exportTransactionsCsv(File file) {
        List<Transaction> tx = new ArrayList<>();
        tx.addAll(manager.expenses().listByMonth(currentMonth));
        tx.addAll(manager.incomes().listByMonth(currentMonth));
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
            new TransactionCsvExporter().export(tx, out);
            status.set("Transactions exported to " + file.getName());
            return true;
        } catch (IOException e) {
            status.set("Export failed: " + e.getMessage());
            return false;
        }
    }

    /** Writes the current month's summary to {@code file} as an Excel workbook. */
    public boolean exportSummaryXlsx(File file) {
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
            new PoiWorkbookExporter().export(manager.summaries().summarize(currentMonth), out);
            status.set("Summary exported to " + file.getName());
            return true;
        } catch (IOException e) {
            status.set("Export failed: " + e.getMessage());
            return false;
        }
    }

    /** Writes the current month's summary to {@code file} as a PDF. */
    public boolean exportSummaryPdf(File file) {
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
            new PdfSummaryExporter().export(manager.summaries().summarize(currentMonth), out);
            status.set("Summary exported to " + file.getName());
            return true;
        } catch (IOException e) {
            status.set("Export failed: " + e.getMessage());
            return false;
        }
    }

    /** Writes the current month's transactions to {@code file} as an Excel workbook. */
    public boolean exportTransactionsXlsx(File file) {
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
            new TransactionWorkbookExporter(manager).export(currentMonth, out);
            status.set("Transactions exported to " + file.getName());
            return true;
        } catch (IOException e) {
            status.set("Export failed: " + e.getMessage());
            return false;
        }
    }

    /** A filename stem for exports, e.g. {@code expense-summary-2026-07}. */
    public String currentMonthStem() {
        return currentMonth.toString();
    }

    public StringProperty monthProperty() { return month; }
    public StringProperty totalIncomeProperty() { return totalIncome; }
    public StringProperty totalExpenseProperty() { return totalExpense; }
    public StringProperty netBalanceProperty() { return netBalance; }
    public StringProperty savingsProperty() { return savings; }
    public StringProperty outstandingProperty() { return outstanding; }
    public StringProperty statusProperty() { return status; }
    public ObservableList<CategoryBreakdownItem> getCategoryBreakdown() { return categoryBreakdown; }
}
