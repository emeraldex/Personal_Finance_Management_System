package com.expense.android.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import com.expense.core.domain.Account;
import com.expense.core.domain.AccountType;
import com.expense.core.domain.Category;
import com.expense.core.domain.CategoryType;
import com.expense.core.domain.Expense;
import com.expense.core.domain.Income;
import com.expense.core.domain.PaymentMethod;
import com.expense.core.domain.PaymentMethodType;
import com.expense.core.domain.Transaction;
import com.expense.core.dto.CreateExpenseRequest;
import com.expense.core.dto.CreateIncomeRequest;
import com.expense.core.dto.UpdateExpenseRequest;
import com.expense.core.dto.UpdateIncomeRequest;
import com.expense.core.network.CategorySuggestion;
import com.expense.core.report.TransactionCsvExporter;
import com.expense.core.service.ExpenseManager;
import com.expense.core.util.Money;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

/**
 * Covers the operations the Android screens gained alongside the desktop parity
 * work — editing transactions, managing reference data, category suggestion and
 * CSV export — running against the real {@code android.database.sqlite} adapters
 * via Robolectric. Every path here is the one the ViewModels call, so a
 * regression in the adapters surfaces without an emulator.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = {34}, manifest = Config.NONE)
public class AndroidEditAndManageTest {

    private final Currency myr = Currency.getInstance("MYR");
    private final YearMonth month = YearMonth.now();
    private final LocalDate today = LocalDate.now();

    private AndroidDatabase db;
    private ExpenseManager manager;

    private long accountId;
    private long groceriesId;
    private long salaryId;
    private long cashMethodId;

    @Before
    public void setUp() {
        Context ctx = RuntimeEnvironment.getApplication();
        File file = ctx.getDatabasePath("edit-" + System.nanoTime() + ".db");
        //noinspection ResultOfMethodCallIgnored
        file.getParentFile().mkdirs();
        db = AndroidDatabase.open(file.getAbsolutePath());

        manager = new ExpenseManager(
                new AndroidCategoryRepository(db),
                new AndroidPaymentMethodRepository(db),
                new AndroidAccountRepository(db),
                new AndroidExpenseRepository(db),
                new AndroidIncomeRepository(db),
                new AndroidBudgetRepository(db),
                new AndroidMonthlySummaryRepository(db),
                myr,
                db);

        Account account = new AndroidAccountRepository(db)
                .save(Account.create("Cash", AccountType.CASH, Money.zero(myr)));
        accountId = account.id();
        groceriesId = new AndroidCategoryRepository(db)
                .save(Category.create("Groceries", CategoryType.EXPENSE, null, null)).id();
        salaryId = new AndroidCategoryRepository(db)
                .save(Category.create("Salary", CategoryType.INCOME, null, null)).id();
        cashMethodId = new AndroidPaymentMethodRepository(db)
                .save(PaymentMethod.create("Cash", PaymentMethodType.CASH)).id();
    }

    @After
    public void tearDown() {
        db.close();
    }

    /** The History screen's edit action: update in place, keeping the sign convention. */
    @Test
    public void editsAnExpenseInPlace() {
        Expense created = manager.expenses().create(new CreateExpenseRequest(
                accountId, groceriesId, cashMethodId, Money.of("120.00", myr), "weekly groceries", today));
        assertNotNull(created.id());

        manager.expenses().update(new UpdateExpenseRequest(
                created.id(), accountId, groceriesId, cashMethodId,
                Money.of("99.50", myr), "weekly groceries (corrected)", today));

        Expense reloaded = manager.expenses().get(created.id());
        // The magnitude is re-normalised to a negative stored amount by the core.
        assertEquals("MYR -99.50", reloaded.amount().toString());
        assertEquals("weekly groceries (corrected)", reloaded.description());
        assertEquals(1, manager.expenses().listByMonth(month).size());

        // The edit flows through to the analytics the dashboard renders.
        assertEquals("MYR -99.50", manager.summaries().summarize(month).totalExpense().toString());
    }

    /** The same edit path for income, which has no payment method. */
    @Test
    public void editsAnIncomeInPlace() {
        Income created = manager.incomes().create(new CreateIncomeRequest(
                accountId, salaryId, Money.of("3000.00", myr), "salary", today));

        manager.incomes().update(new UpdateIncomeRequest(
                created.id(), accountId, salaryId, Money.of("3500.00", myr), "salary + bonus", today));

        Income reloaded = manager.incomes().get(created.id());
        assertEquals("MYR 3500.00", reloaded.amount().toString());
        assertEquals("salary + bonus", reloaded.description());
    }

    /**
     * The Manage screen's rename and archive actions. Archiving must hide an entry
     * from the pickers while leaving the transactions that reference it intact —
     * that is the whole reason it is archiving rather than deletion.
     */
    @Test
    public void renamesAndArchivesWithoutLosingHistory() {
        Expense expense = manager.expenses().create(new CreateExpenseRequest(
                accountId, groceriesId, cashMethodId, Money.of("40.00", myr), "market", today));

        manager.categories().rename(groceriesId, "Food & groceries");
        assertEquals("Food & groceries", manager.categories().get(groceriesId).name());

        manager.categories().setArchived(groceriesId, true);
        // Hidden from the entry pickers...
        assertTrue(manager.categories().listByType(CategoryType.EXPENSE).isEmpty());
        // ...but still present for name resolution in History, and still referenced.
        assertTrue(manager.categories().list().stream().anyMatch(Category::archived));
        assertEquals(Long.valueOf(groceriesId),
                manager.expenses().get(expense.id()).categoryId());

        manager.categories().setArchived(groceriesId, false);
        assertEquals(1, manager.categories().listByType(CategoryType.EXPENSE).size());

        // Accounts and payment methods archive the same way.
        manager.accounts().setArchived(accountId, true);
        assertTrue(manager.accounts().get(accountId).archived());
        manager.paymentMethods().setArchived(cashMethodId, true);
        assertTrue(manager.paymentMethods().get(cashMethodId).archived());
        assertFalse(manager.paymentMethods().list().isEmpty());
    }

    /** Settings' CSV export, rendered by the shared core exporter over Android-backed data. */
    @Test
    public void rendersTheMonthAsCsv() throws IOException {
        manager.expenses().create(new CreateExpenseRequest(
                accountId, groceriesId, cashMethodId, Money.of("120.00", myr), "weekly groceries", today));
        manager.incomes().create(new CreateIncomeRequest(
                accountId, salaryId, Money.of("3000.00", myr), "salary", today));

        List<Transaction> all = new ArrayList<>();
        all.addAll(manager.expenses().listByMonth(month));
        all.addAll(manager.incomes().listByMonth(month));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new TransactionCsvExporter().export(all, out);
        String csv = new String(out.toByteArray(), StandardCharsets.UTF_8);

        assertTrue(csv.startsWith("Id,Date,Type,Amount,Currency"));
        assertTrue(csv.contains("weekly groceries"));
        assertTrue(csv.contains("salary"));
        assertTrue(csv.contains("-120.00"));
        assertTrue(csv.contains("3000.00"));
        // Header plus one row per transaction.
        assertEquals(3, csv.trim().split("\n").length);
    }

    /** The Add-Expense screen's auto-categorise hint, served by the core categoriser. */
    @Test
    public void suggestsACategoryFromTheDescription() {
        List<Category> candidates = manager.categories().listByType(CategoryType.EXPENSE);
        Optional<CategorySuggestion> suggestion =
                manager.categorizer().suggest("weekly groceries run", candidates);

        assertTrue(suggestion.isPresent());
        assertEquals(groceriesId, suggestion.get().categoryId());
        assertTrue(suggestion.get().confidence() > 0.25);

        // Nothing plausible: no suggestion rather than a wrong one.
        assertFalse(manager.categorizer().suggest("zzzz qqqq", candidates).isPresent());
    }
}
