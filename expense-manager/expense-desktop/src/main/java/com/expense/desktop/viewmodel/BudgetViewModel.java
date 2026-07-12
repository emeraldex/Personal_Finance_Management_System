package com.expense.desktop.viewmodel;

import com.expense.core.domain.Budget;
import com.expense.core.domain.Category;
import com.expense.core.domain.CategoryType;
import com.expense.core.dto.CreateBudgetRequest;
import com.expense.core.exception.ExpenseException;
import com.expense.core.report.BudgetUtilization;
import com.expense.core.service.ExpenseManager;
import com.expense.core.util.Money;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Currency;
import java.util.Objects;

/**
 * ViewModel for the budgets tab. For the selected month it lists each category's
 * budget-versus-actual (from the core's computed {@link BudgetUtilization}) and
 * lets the user set or delete a per-category monthly cap via {@code BudgetService}.
 */
public final class BudgetViewModel {

    private final ExpenseManager manager;
    private final Currency currency;
    private final Runnable onDataChanged;

    private YearMonth currentMonth = YearMonth.now();

    private final ObservableList<BudgetUtilization> utilizations = FXCollections.observableArrayList();
    private final ObservableList<Category> expenseCategories = FXCollections.observableArrayList();
    private final StringProperty month = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty("");

    public BudgetViewModel(ExpenseManager manager, Runnable onDataChanged) {
        this.manager = Objects.requireNonNull(manager);
        this.currency = manager.defaultCurrency();
        this.onDataChanged = onDataChanged == null ? () -> { } : onDataChanged;
        reload();
    }

    public void reload() {
        month.set(currentMonth.toString());
        expenseCategories.setAll(manager.categories().listByType(CategoryType.EXPENSE));
        utilizations.setAll(manager.summaries().summarize(currentMonth).budgetUtilization());
    }

    public void nextMonth() {
        currentMonth = currentMonth.plusMonths(1);
        reload();
    }

    public void prevMonth() {
        currentMonth = currentMonth.minusMonths(1);
        reload();
    }

    /** Sets (creates or replaces) a monthly cap for {@code category}. */
    public boolean setBudget(Category category, String limitText) {
        if (category == null) {
            status.set("Please choose a category");
            return false;
        }
        try {
            Money limit = Money.of(new BigDecimal(limitText.trim()), currency);
            manager.budgets().set(new CreateBudgetRequest(category.id(), currentMonth, limit));
            status.set("Budget set for " + category.name());
            reload();
            onDataChanged.run();
            return true;
        } catch (NumberFormatException e) {
            status.set("Limit must be a number");
            return false;
        } catch (ExpenseException e) {
            status.set(e.getMessage());
            return false;
        }
    }

    /** Deletes the budget backing the selected utilisation row. */
    public boolean deleteSelected(BudgetUtilization selected) {
        if (selected == null) {
            status.set("Select a budget to delete");
            return false;
        }
        try {
            for (Budget b : manager.budgets().listForMonth(currentMonth)) {
                if (b.categoryId() == selected.categoryId() && b.id() != null) {
                    manager.budgets().delete(b.id());
                    status.set("Budget removed for " + selected.categoryName());
                    reload();
                    onDataChanged.run();
                    return true;
                }
            }
            status.set("No stored budget found for " + selected.categoryName());
            return false;
        } catch (ExpenseException e) {
            status.set(e.getMessage());
            return false;
        }
    }

    public ObservableList<BudgetUtilization> getUtilizations() { return utilizations; }
    public ObservableList<Category> getExpenseCategories() { return expenseCategories; }
    public StringProperty monthProperty() { return month; }
    public StringProperty statusProperty() { return status; }
}
