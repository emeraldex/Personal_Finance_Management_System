package com.expense.core.service;

import com.expense.core.database.Database;
import com.expense.core.network.ExpenseCategorizer;
import com.expense.core.network.HeuristicExpenseCategorizer;
import com.expense.core.repository.AccountRepository;
import com.expense.core.repository.BudgetRepository;
import com.expense.core.repository.CategoryRepository;
import com.expense.core.repository.ExpenseRepository;
import com.expense.core.repository.IncomeRepository;
import com.expense.core.repository.JdbcAccountRepository;
import com.expense.core.repository.JdbcBudgetRepository;
import com.expense.core.repository.JdbcCategoryRepository;
import com.expense.core.repository.JdbcExpenseRepository;
import com.expense.core.repository.JdbcIncomeRepository;
import com.expense.core.repository.JdbcMonthlySummaryRepository;
import com.expense.core.repository.JdbcPaymentMethodRepository;
import com.expense.core.repository.MonthlySummaryRepository;
import com.expense.core.repository.PaymentMethodRepository;

import java.util.Currency;
import java.util.Objects;

/**
 * Single composition root and entry point for the core library.
 *
 * <p>UI modules (JavaFX desktop, Jetpack Compose Android) depend on this class
 * only. It owns the {@link Database}, constructs the JDBC repositories, and wires
 * them into the business services via constructor injection. This is a
 * deliberately small, hand-rolled dependency-injection container: the core has no
 * framework dependency, yet every collaborator is injected and therefore testable
 * in isolation.
 *
 * <p>Instances are created through the static factories {@link #openFile(String, Currency)}
 * and {@link #inMemory(Currency)}. The manager is {@link AutoCloseable}; closing it
 * closes the underlying database connection.
 *
 * <pre>{@code
 * try (ExpenseManager app = ExpenseManager.openFile("finance.db", Currency.getInstance("MYR"))) {
 *     long accountId = app.accounts().create(new CreateAccountRequest(...)).id();
 *     app.expenses().create(new CreateExpenseRequest(...));
 *     MonthlySummary july = app.summaries().summarize(YearMonth.of(2026, 7));
 * }
 * }</pre>
 */
public final class ExpenseManager implements AutoCloseable {

    private final Database database;
    private final Currency defaultCurrency;

    private final CategoryService categoryService;
    private final PaymentMethodService paymentMethodService;
    private final AccountService accountService;
    private final ExpenseService expenseService;
    private final IncomeService incomeService;
    private final BudgetService budgetService;
    private final MonthlySummaryService summaryService;
    private final ExpenseCategorizer categorizer;

    /**
     * Constructs a manager over an already-configured {@link Database}. Prefer the
     * {@link #openFile(String, Currency)} / {@link #inMemory(Currency)} factories for
     * normal use; this constructor exists so callers (and tests) can inject a
     * database they own and control the lifecycle themselves.
     *
     * @param database        an initialised database (schema already applied)
     * @param defaultCurrency currency used when a month has no transactions to infer one from
     */
    public ExpenseManager(Database database, Currency defaultCurrency) {
        this.database = Objects.requireNonNull(database, "database");
        this.defaultCurrency = Objects.requireNonNull(defaultCurrency, "defaultCurrency");

        // Repositories (share the single connection provider).
        CategoryRepository categories = new JdbcCategoryRepository(database);
        PaymentMethodRepository paymentMethods = new JdbcPaymentMethodRepository(database);
        AccountRepository accounts = new JdbcAccountRepository(database);
        ExpenseRepository expenses = new JdbcExpenseRepository(database);
        IncomeRepository incomes = new JdbcIncomeRepository(database);
        BudgetRepository budgets = new JdbcBudgetRepository(database);
        MonthlySummaryRepository summaries = new JdbcMonthlySummaryRepository(database);

        // Services (business logic; UI-independent).
        this.categoryService = new CategoryService(categories);
        this.paymentMethodService = new PaymentMethodService(paymentMethods);
        this.accountService = new AccountService(accounts);
        this.expenseService = new ExpenseService(expenses, accounts, categories, paymentMethods);
        this.incomeService = new IncomeService(incomes, accounts, categories);
        this.budgetService = new BudgetService(budgets, categories);
        this.summaryService = new MonthlySummaryService(
                expenses, incomes, categories, paymentMethods, budgets, summaries, defaultCurrency);
        this.categorizer = new HeuristicExpenseCategorizer();
    }

    /**
     * Opens (creating if necessary) a file-backed database and initialises the schema.
     *
     * @param filePath        path to the SQLite database file
     * @param defaultCurrency currency used when a month has no transactions to infer one from
     */
    public static ExpenseManager openFile(String filePath, Currency defaultCurrency) {
        return new ExpenseManager(Database.openFile(filePath), defaultCurrency);
    }

    /**
     * Opens an in-memory database (schema initialised). Primarily for tests and demos.
     */
    public static ExpenseManager inMemory(Currency defaultCurrency) {
        return new ExpenseManager(Database.openInMemory(), defaultCurrency);
    }

    public CategoryService categories() {
        return categoryService;
    }

    public PaymentMethodService paymentMethods() {
        return paymentMethodService;
    }

    public AccountService accounts() {
        return accountService;
    }

    public ExpenseService expenses() {
        return expenseService;
    }

    public IncomeService incomes() {
        return incomeService;
    }

    public BudgetService budgets() {
        return budgetService;
    }

    public MonthlySummaryService summaries() {
        return summaryService;
    }

    /** The default offline categoriser; a smarter implementation can be swapped in later. */
    public ExpenseCategorizer categorizer() {
        return categorizer;
    }

    /** The default currency used for empty-month summaries. */
    public Currency defaultCurrency() {
        return defaultCurrency;
    }

    @Override
    public void close() {
        database.close();
    }
}
