package com.expense.desktop.viewmodel;

import com.expense.core.domain.Account;
import com.expense.core.domain.Category;
import com.expense.core.domain.CategoryType;
import com.expense.core.dto.CreateIncomeRequest;
import com.expense.core.exception.ExpenseException;
import com.expense.core.service.ExpenseManager;
import com.expense.core.util.Money;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Objects;

/**
 * ViewModel backing the "Add Income" form. Mirrors {@link ExpenseFormViewModel}
 * but targets income: it offers only INCOME-typed categories, has no payment
 * method, and delegates the save to the core {@code IncomeService}. After a
 * successful save it runs {@code onSaved} so the dashboard and history refresh.
 */
public final class IncomeFormViewModel {

    private final ExpenseManager manager;
    private final Currency currency;
    private final Runnable onSaved;

    private final StringProperty amount = new SimpleStringProperty("");
    private final StringProperty description = new SimpleStringProperty("");
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>(LocalDate.now());
    private final ObjectProperty<Account> account = new SimpleObjectProperty<>();
    private final ObjectProperty<Category> category = new SimpleObjectProperty<>();
    private final StringProperty status = new SimpleStringProperty("");

    private final ObservableList<Account> accounts = FXCollections.observableArrayList();
    private final ObservableList<Category> categories = FXCollections.observableArrayList();

    public IncomeFormViewModel(ExpenseManager manager, Runnable onSaved) {
        this.manager = Objects.requireNonNull(manager);
        this.currency = manager.defaultCurrency();
        this.onSaved = onSaved == null ? () -> { } : onSaved;
        refreshLookups();
    }

    /** Reloads accounts and income categories, excluding archived ones. */
    public void refreshLookups() {
        accounts.setAll(manager.accounts().list().stream().filter(a -> !a.archived()).toList());
        categories.setAll(manager.categories().listByType(CategoryType.INCOME).stream()
                .filter(c -> !c.archived()).toList());
    }

    /**
     * Attempts to save the current form as income.
     *
     * @return {@code true} on success; on failure {@link #statusProperty()} holds the message
     */
    public boolean save() {
        try {
            if (account.get() == null) {
                status.set("Please choose an account");
                return false;
            }
            Money money = Money.of(new BigDecimal(amount.get().trim()), currency);
            Long categoryId = category.get() == null ? null : category.get().id();
            manager.incomes().create(new CreateIncomeRequest(
                    account.get().id(), categoryId, money, description.get(), date.get()));
            status.set("Saved");
            amount.set("");
            description.set("");
            onSaved.run();
            return true;
        } catch (NumberFormatException e) {
            status.set("Amount must be a number");
            return false;
        } catch (ExpenseException e) {
            status.set(e.getMessage());
            return false;
        }
    }

    public StringProperty amountProperty() { return amount; }
    public StringProperty descriptionProperty() { return description; }
    public ObjectProperty<LocalDate> dateProperty() { return date; }
    public ObjectProperty<Account> accountProperty() { return account; }
    public ObjectProperty<Category> categoryProperty() { return category; }
    public StringProperty statusProperty() { return status; }
    public ObservableList<Account> getAccounts() { return accounts; }
    public ObservableList<Category> getCategories() { return categories; }
}
