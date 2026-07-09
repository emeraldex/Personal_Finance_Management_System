package com.expense.desktop.viewmodel;

import com.expense.core.report.CategoryBreakdownItem;
import com.expense.core.report.MonthlySummary;
import com.expense.core.service.ExpenseManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.YearMonth;
import java.util.Objects;

/**
 * ViewModel for the dashboard. Exposes JavaFX observable properties the View
 * binds to; it never references any JavaFX node, keeping it headless-testable.
 * All data comes from the injected {@link ExpenseManager}.
 */
public final class DashboardViewModel {

    private final ExpenseManager manager;

    private final StringProperty month = new SimpleStringProperty();
    private final StringProperty totalIncome = new SimpleStringProperty("-");
    private final StringProperty totalExpense = new SimpleStringProperty("-");
    private final StringProperty netBalance = new SimpleStringProperty("-");
    private final StringProperty savings = new SimpleStringProperty("-");
    private final StringProperty outstanding = new SimpleStringProperty("-");
    private final ObservableList<CategoryBreakdownItem> categoryBreakdown =
            FXCollections.observableArrayList();

    public DashboardViewModel(ExpenseManager manager) {
        this.manager = Objects.requireNonNull(manager);
    }

    /** Loads the summary for the current calendar month. */
    public void loadCurrentMonth() {
        load(YearMonth.now());
    }

    /** Loads and publishes the summary for {@code ym}. */
    public void load(YearMonth ym) {
        MonthlySummary s = manager.summaries().summarize(ym);
        month.set(ym.toString());
        totalIncome.set(s.totalIncome().toString());
        totalExpense.set(s.totalExpense().toString());
        netBalance.set(s.netBalance().toString());
        savings.set(s.savings().toString());
        outstanding.set(s.outstanding().toString());
        categoryBreakdown.setAll(s.categoryBreakdown());
    }

    public StringProperty monthProperty() { return month; }
    public StringProperty totalIncomeProperty() { return totalIncome; }
    public StringProperty totalExpenseProperty() { return totalExpense; }
    public StringProperty netBalanceProperty() { return netBalance; }
    public StringProperty savingsProperty() { return savings; }
    public StringProperty outstandingProperty() { return outstanding; }
    public ObservableList<CategoryBreakdownItem> getCategoryBreakdown() { return categoryBreakdown; }
}
