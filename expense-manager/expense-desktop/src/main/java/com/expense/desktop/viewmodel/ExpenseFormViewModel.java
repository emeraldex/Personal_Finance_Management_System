package com.expense.desktop.viewmodel;

import com.expense.core.domain.Account;
import com.expense.core.domain.Category;
import com.expense.core.domain.CategoryType;
import com.expense.core.domain.PaymentMethod;
import com.expense.core.dto.CreateExpenseRequest;
import com.expense.core.exception.ValidationException;
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
 * ViewModel backing the "Add Expense" form. Holds editable fields as observable
 * properties, exposes selectable master-data lists, and performs the save by
 * delegating to the core {@code ExpenseService}. Validation errors surface via
 * {@link #statusProperty()} so the View can display them without knowing the
 * validation rules.
 */
public final class ExpenseFormViewModel {

    private final ExpenseManager manager;
    private final Currency currency;

    private final StringProperty amount = new SimpleStringProperty("");
    private final StringProperty description = new SimpleStringProperty("");
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>(LocalDate.now());
    private final ObjectProperty<Account> account = new SimpleObjectProperty<>();
    private final ObjectProperty<Category> category = new SimpleObjectProperty<>();
    private final ObjectProperty<PaymentMethod> paymentMethod = new SimpleObjectProperty<>();
    private final StringProperty status = new SimpleStringProperty("");

    private final ObservableList<Account> accounts = FXCollections.observableArrayList();
    private final ObservableList<Category> categories = FXCollections.observableArrayList();
    private final ObservableList<PaymentMethod> paymentMethods = FXCollections.observableArrayList();

    public ExpenseFormViewModel(ExpenseManager manager) {
        this.manager = Objects.requireNonNull(manager);
        this.currency = manager.defaultCurrency();
        refreshLookups();
    }

    /** Reloads accounts / expense categories / payment methods from the core. */
    public void refreshLookups() {
        accounts.setAll(manager.accounts().list());
        categories.setAll(manager.categories().listByType(CategoryType.EXPENSE));
        paymentMethods.setAll(manager.paymentMethods().list());
    }

    /**
     * Attempts to save the current form as an expense.
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
            Long pmId = paymentMethod.get() == null ? null : paymentMethod.get().id();
            manager.expenses().create(new CreateExpenseRequest(
                    account.get().id(), categoryId, pmId, money, description.get(), date.get()));
            status.set("Saved");
            amount.set("");
            description.set("");
            return true;
        } catch (NumberFormatException e) {
            status.set("Amount must be a number");
            return false;
        } catch (ValidationException e) {
            status.set(e.errors().toString());
            return false;
        }
    }

    public StringProperty amountProperty() { return amount; }
    public StringProperty descriptionProperty() { return description; }
    public ObjectProperty<LocalDate> dateProperty() { return date; }
    public ObjectProperty<Account> accountProperty() { return account; }
    public ObjectProperty<Category> categoryProperty() { return category; }
    public ObjectProperty<PaymentMethod> paymentMethodProperty() { return paymentMethod; }
    public StringProperty statusProperty() { return status; }
    public ObservableList<Account> getAccounts() { return accounts; }
    public ObservableList<Category> getCategories() { return categories; }
    public ObservableList<PaymentMethod> getPaymentMethods() { return paymentMethods; }
}
