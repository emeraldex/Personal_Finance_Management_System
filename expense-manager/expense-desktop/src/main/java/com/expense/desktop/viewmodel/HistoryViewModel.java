package com.expense.desktop.viewmodel;

import com.expense.core.domain.Account;
import com.expense.core.domain.Category;
import com.expense.core.domain.CategoryType;
import com.expense.core.domain.Expense;
import com.expense.core.domain.Income;
import com.expense.core.domain.PaymentMethod;
import com.expense.core.domain.Transaction;
import com.expense.core.dto.UpdateExpenseRequest;
import com.expense.core.dto.UpdateIncomeRequest;
import com.expense.core.exception.ExpenseException;
import com.expense.core.service.ExpenseManager;
import com.expense.core.util.Money;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * ViewModel for the transaction-history tab. Lists a month's expenses and income
 * as a unified, display-ready {@link Row} list and supports editing and deleting
 * an individual transaction by delegating to the core services. After any change
 * it reloads and runs {@code onDataChanged} so the dashboard and budgets refresh.
 */
public final class HistoryViewModel {

    /** A display-ready row that also carries the underlying {@link Transaction} for editing. */
    public record Row(Transaction tx, String date, String type, String amount,
                      String category, String account, String paymentMethod, String description) {
    }

    private final ExpenseManager manager;
    private final Currency currency;
    private final Runnable onDataChanged;

    private YearMonth currentMonth = YearMonth.now();

    private final ObservableList<Row> rows = FXCollections.observableArrayList();
    private final ObservableList<Account> accounts = FXCollections.observableArrayList();
    private final ObservableList<Category> expenseCategories = FXCollections.observableArrayList();
    private final ObservableList<Category> incomeCategories = FXCollections.observableArrayList();
    private final ObservableList<PaymentMethod> paymentMethods = FXCollections.observableArrayList();
    private final StringProperty month = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty("");

    public HistoryViewModel(ExpenseManager manager, Runnable onDataChanged) {
        this.manager = Objects.requireNonNull(manager);
        this.currency = manager.defaultCurrency();
        this.onDataChanged = onDataChanged == null ? () -> { } : onDataChanged;
        reload();
    }

    public void reload() {
        accounts.setAll(manager.accounts().list());
        expenseCategories.setAll(manager.categories().listByType(CategoryType.EXPENSE));
        incomeCategories.setAll(manager.categories().listByType(CategoryType.INCOME));
        paymentMethods.setAll(manager.paymentMethods().list());
        month.set(currentMonth.toString());

        Map<Long, String> accountNames = new HashMap<>();
        for (Account a : accounts) {
            accountNames.put(a.id(), a.name());
        }
        Map<Long, String> categoryNames = new HashMap<>();
        for (Category c : manager.categories().list()) {
            categoryNames.put(c.id(), c.name());
        }
        Map<Long, String> paymentNames = new HashMap<>();
        for (PaymentMethod p : paymentMethods) {
            paymentNames.put(p.id(), p.name());
        }

        List<Expense> monthExpenses = manager.expenses().listByMonth(currentMonth);
        List<Income> monthIncomes = manager.incomes().listByMonth(currentMonth);

        List<Row> built = new java.util.ArrayList<>();
        for (Expense e : monthExpenses) {
            built.add(new Row(e, e.date().toString(), "Expense",
                    e.signedAmount().toString(),
                    name(categoryNames, e.categoryId(), "Uncategorised"),
                    accountNames.getOrDefault(e.accountId(), "?"),
                    name(paymentNames, e.paymentMethodId(), ""),
                    e.description()));
        }
        for (Income i : monthIncomes) {
            built.add(new Row(i, i.date().toString(), "Income",
                    i.signedAmount().toString(),
                    name(categoryNames, i.categoryId(), "Uncategorised"),
                    accountNames.getOrDefault(i.accountId(), "?"),
                    "",
                    i.description()));
        }
        built.sort(Comparator.comparing((Row r) -> r.tx().date()).reversed()
                .thenComparing(r -> r.type()));
        rows.setAll(built);
    }

    public void nextMonth() {
        currentMonth = currentMonth.plusMonths(1);
        reload();
    }

    public void prevMonth() {
        currentMonth = currentMonth.minusMonths(1);
        reload();
    }

    /** Deletes the given transaction, dispatching by its concrete type. */
    public boolean delete(Transaction tx) {
        if (tx == null) {
            status.set("Select a transaction to delete");
            return false;
        }
        try {
            if (tx instanceof Expense e) {
                manager.expenses().delete(e.id());
            } else if (tx instanceof Income i) {
                manager.incomes().delete(i.id());
            }
            status.set("Deleted");
            reload();
            onDataChanged.run();
            return true;
        } catch (ExpenseException ex) {
            status.set(ex.getMessage());
            return false;
        }
    }

    /**
     * Applies an edit to the selected transaction. {@code paymentMethod} is ignored
     * for income. The amount is a positive magnitude; the core normalises its sign.
     */
    public boolean saveEdit(Transaction original, Account account, Category category,
                            PaymentMethod paymentMethod, String amountText,
                            String description, LocalDate date) {
        if (original == null) {
            status.set("Select a transaction to edit");
            return false;
        }
        if (account == null) {
            status.set("Please choose an account");
            return false;
        }
        try {
            Money money = Money.of(new BigDecimal(amountText.trim()), currency);
            Long categoryId = category == null ? null : category.id();
            if (original instanceof Expense e) {
                Long pmId = paymentMethod == null ? null : paymentMethod.id();
                manager.expenses().update(new UpdateExpenseRequest(
                        e.id(), account.id(), categoryId, pmId, money, description, date));
            } else if (original instanceof Income i) {
                manager.incomes().update(new UpdateIncomeRequest(
                        i.id(), account.id(), categoryId, money, description, date));
            }
            status.set("Updated");
            reload();
            onDataChanged.run();
            return true;
        } catch (NumberFormatException ex) {
            status.set("Amount must be a number");
            return false;
        } catch (ExpenseException ex) {
            status.set(ex.getMessage());
            return false;
        }
    }

    private static String name(Map<Long, String> names, Long id, String fallback) {
        if (id == null) {
            return fallback;
        }
        return names.getOrDefault(id, "?");
    }

    public ObservableList<Row> getRows() { return rows; }
    public ObservableList<Account> getAccounts() { return accounts; }
    public ObservableList<Category> getExpenseCategories() { return expenseCategories; }
    public ObservableList<Category> getIncomeCategories() { return incomeCategories; }
    public ObservableList<PaymentMethod> getPaymentMethods() { return paymentMethods; }
    public StringProperty monthProperty() { return month; }
    public StringProperty statusProperty() { return status; }
}
