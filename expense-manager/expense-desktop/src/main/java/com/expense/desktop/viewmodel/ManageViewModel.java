package com.expense.desktop.viewmodel;

import com.expense.core.domain.Account;
import com.expense.core.domain.AccountType;
import com.expense.core.domain.Category;
import com.expense.core.domain.CategoryType;
import com.expense.core.domain.PaymentMethod;
import com.expense.core.domain.PaymentMethodType;
import com.expense.core.dto.CreateAccountRequest;
import com.expense.core.dto.CreateCategoryRequest;
import com.expense.core.dto.CreatePaymentMethodRequest;
import com.expense.core.exception.ExpenseException;
import com.expense.core.service.ExpenseManager;
import com.expense.core.util.Money;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

/**
 * ViewModel backing the "Manage" tab. Exposes the full account / category /
 * payment-method lists as observable data and performs create and delete
 * operations by delegating to the core services. Domain errors are converted to
 * a human-readable {@link #statusProperty()} message so the View stays logic-free.
 *
 * <p>After any successful mutation it runs the {@code onDataChanged} callback so
 * dependent screens (e.g. the Add-Expense pickers) can refresh their lookups.</p>
 */
public final class ManageViewModel {

    private final ExpenseManager manager;
    private final Currency currency;
    private final Runnable onDataChanged;

    private final ObservableList<Account> accounts = FXCollections.observableArrayList();
    private final ObservableList<Category> categories = FXCollections.observableArrayList();
    private final ObservableList<PaymentMethod> paymentMethods = FXCollections.observableArrayList();
    private final StringProperty status = new SimpleStringProperty("");

    public ManageViewModel(ExpenseManager manager, Currency currency, Runnable onDataChanged) {
        this.manager = Objects.requireNonNull(manager);
        this.currency = Objects.requireNonNull(currency);
        this.onDataChanged = onDataChanged == null ? () -> { } : onDataChanged;
        refresh();
    }

    /** Reloads all three lists from the core. */
    public void refresh() {
        accounts.setAll(manager.accounts().list());
        categories.setAll(manager.categories().list());
        paymentMethods.setAll(manager.paymentMethods().list());
    }

    // ---- Accounts -------------------------------------------------------

    public boolean createAccount(String name, AccountType type, String openingBalanceText) {
        try {
            String raw = openingBalanceText == null || openingBalanceText.isBlank()
                    ? "0" : openingBalanceText.trim();
            Money opening = Money.of(new BigDecimal(raw), currency);
            manager.accounts().create(new CreateAccountRequest(name, type, opening));
            return succeed("Account created: " + name);
        } catch (NumberFormatException e) {
            return fail("Opening balance must be a number");
        } catch (ExpenseException e) {
            return fail(e.getMessage());
        }
    }

    public boolean deleteAccount(Account account) {
        if (account == null) {
            return fail("Select an account to delete");
        }
        try {
            manager.accounts().delete(account.id());
            return succeed("Account deleted: " + account.name());
        } catch (ExpenseException e) {
            return fail("Cannot delete '" + account.name()
                    + "'. It may still have transactions — archive it instead.");
        }
    }

    // ---- Categories -----------------------------------------------------

    public boolean createCategory(String name, CategoryType type) {
        try {
            manager.categories().create(new CreateCategoryRequest(name, type, null, null));
            return succeed("Category created: " + name);
        } catch (ExpenseException e) {
            return fail(e.getMessage());
        }
    }

    public boolean deleteCategory(Category category) {
        if (category == null) {
            return fail("Select a category to delete");
        }
        try {
            manager.categories().delete(category.id());
            return succeed("Category deleted: " + category.name());
        } catch (ExpenseException e) {
            return fail(e.getMessage());
        }
    }

    // ---- Payment methods ------------------------------------------------

    public boolean createPaymentMethod(String name, PaymentMethodType type) {
        try {
            manager.paymentMethods().create(new CreatePaymentMethodRequest(name, type));
            return succeed("Payment method created: " + name);
        } catch (ExpenseException e) {
            return fail(e.getMessage());
        }
    }

    public boolean deletePaymentMethod(PaymentMethod method) {
        if (method == null) {
            return fail("Select a payment method to delete");
        }
        try {
            manager.paymentMethods().delete(method.id());
            return succeed("Payment method deleted: " + method.name());
        } catch (ExpenseException e) {
            return fail("Cannot delete '" + method.name()
                    + "'. It may still be used by transactions — archive it instead.");
        }
    }

    // ---- helpers --------------------------------------------------------

    private boolean succeed(String message) {
        refresh();
        onDataChanged.run();
        status.set(message);
        return true;
    }

    private boolean fail(String message) {
        status.set(message);
        return false;
    }

    public ObservableList<Account> getAccounts() { return accounts; }
    public ObservableList<Category> getCategories() { return categories; }
    public ObservableList<PaymentMethod> getPaymentMethods() { return paymentMethods; }
    public StringProperty statusProperty() { return status; }
}
