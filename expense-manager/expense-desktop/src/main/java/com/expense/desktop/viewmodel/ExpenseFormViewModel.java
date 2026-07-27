package com.expense.desktop.viewmodel;

import com.expense.core.domain.Account;
import com.expense.core.domain.Category;
import com.expense.core.domain.CategoryType;
import com.expense.core.domain.PaymentMethod;
import com.expense.core.dto.CreateExpenseRequest;
import com.expense.core.exception.ValidationException;
import com.expense.core.service.BudgetAlertService;
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
import java.time.YearMonth;
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
    private final Runnable onSaved;
    private final java.util.function.BooleanSupplier autoCategorize;
    private final BudgetAlertService budgetAlerts;

    private final StringProperty amount = new SimpleStringProperty("");
    private final ObjectProperty<Currency> entryCurrency = new SimpleObjectProperty<>();
    private final StringProperty description = new SimpleStringProperty("");
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>(LocalDate.now());
    private final ObjectProperty<Account> account = new SimpleObjectProperty<>();
    private final ObjectProperty<Category> category = new SimpleObjectProperty<>();
    private final ObjectProperty<PaymentMethod> paymentMethod = new SimpleObjectProperty<>();
    private final StringProperty status = new SimpleStringProperty("");

    private final ObservableList<Account> accounts = FXCollections.observableArrayList();
    private final ObservableList<Category> categories = FXCollections.observableArrayList();
    private final ObservableList<PaymentMethod> paymentMethods = FXCollections.observableArrayList();
    private final ObservableList<Currency> currencies = FXCollections.observableArrayList();

    /**
     * @param budgetAlerts checks the saved expense's budget and publishes a
     *                     notification when warranted; {@code null} disables alerts
     */
    public ExpenseFormViewModel(ExpenseManager manager, Runnable onSaved,
                                java.util.function.BooleanSupplier autoCategorize,
                                BudgetAlertService budgetAlerts) {
        this.manager = Objects.requireNonNull(manager);
        this.currency = manager.defaultCurrency();
        this.onSaved = onSaved == null ? () -> { } : onSaved;
        this.autoCategorize = autoCategorize == null ? () -> false : autoCategorize;
        this.budgetAlerts = budgetAlerts;
        this.entryCurrency.set(currency);
        refreshLookups();
    }

    /**
     * Reloads accounts / expense categories / payment methods (excluding archived
     * ones) and the entry currencies: the app currency plus every currency with an
     * exchange rate into it.
     */
    public void refreshLookups() {
        accounts.setAll(manager.accounts().list().stream().filter(a -> !a.archived()).toList());
        categories.setAll(manager.categories().listByType(CategoryType.EXPENSE).stream()
                .filter(c -> !c.archived()).toList());
        paymentMethods.setAll(manager.paymentMethods().list().stream()
                .filter(p -> !p.archived()).toList());
        var options = new java.util.ArrayList<Currency>();
        options.add(currency);
        options.addAll(manager.exchangeRates().sourcesFor(currency));
        currencies.setAll(options);
        if (!currencies.contains(entryCurrency.get())) {
            entryCurrency.set(currency);
        }
    }

    /**
     * Uses the core's offline categoriser to suggest and select a category from the
     * current description. No-op with a status note when nothing is confident enough.
     *
     * @return {@code true} if a category was selected
     */
    public boolean suggestCategory() {
        var suggestion = manager.categorizer().suggest(description.get(), categories);
        if (suggestion.isEmpty()) {
            status.set("No category suggestion");
            return false;
        }
        long suggestedId = suggestion.get().categoryId();
        for (Category c : categories) {
            if (c.id() != null && c.id() == suggestedId) {
                category.set(c);
                status.set("Suggested category: " + c.name());
                return true;
            }
        }
        return false;
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
            if (category.get() == null && autoCategorize.getAsBoolean()) {
                suggestCategory();
            }
            Money entered = Money.of(new BigDecimal(amount.get().trim()),
                    entryCurrency.get() == null ? currency : entryCurrency.get());
            Money money;
            if (entered.currency().equals(currency)) {
                money = entered;
            } else {
                var converted = manager.conversions().convert(entered, currency, date.get());
                if (converted.isEmpty()) {
                    status.set("No exchange rate for " + entered.currency().getCurrencyCode()
                            + " — set one in Settings");
                    return false;
                }
                money = converted.get();
            }
            Long categoryId = category.get() == null ? null : category.get().id();
            Long pmId = paymentMethod.get() == null ? null : paymentMethod.get().id();
            manager.expenses().create(new CreateExpenseRequest(
                    account.get().id(), categoryId, pmId, money, description.get(), date.get()));
            status.set(entered.currency().equals(currency)
                    ? "Saved"
                    : "Saved (" + entered + " → " + money + ")");
            if (budgetAlerts != null) {
                budgetAlerts.checkAfterExpenseChange(YearMonth.from(date.get()), categoryId)
                        .ifPresent(n -> status.set("Saved — " + n.title().toLowerCase() + ": "
                                + n.body()));
            }
            amount.set("");
            description.set("");
            onSaved.run();
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
    public ObjectProperty<Currency> entryCurrencyProperty() { return entryCurrency; }
    public StringProperty descriptionProperty() { return description; }
    public ObjectProperty<LocalDate> dateProperty() { return date; }
    public ObjectProperty<Account> accountProperty() { return account; }
    public ObjectProperty<Category> categoryProperty() { return category; }
    public ObjectProperty<PaymentMethod> paymentMethodProperty() { return paymentMethod; }
    public StringProperty statusProperty() { return status; }
    public ObservableList<Account> getAccounts() { return accounts; }
    public ObservableList<Category> getCategories() { return categories; }
    public ObservableList<PaymentMethod> getPaymentMethods() { return paymentMethods; }
    public ObservableList<Currency> getCurrencies() { return currencies; }
}
