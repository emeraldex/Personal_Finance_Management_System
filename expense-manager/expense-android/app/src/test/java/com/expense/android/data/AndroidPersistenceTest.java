package com.expense.android.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import com.expense.core.domain.Account;
import com.expense.core.domain.AccountType;
import com.expense.core.domain.Budget;
import com.expense.core.domain.Category;
import com.expense.core.domain.CategoryType;
import com.expense.core.domain.Expense;
import com.expense.core.domain.Income;
import com.expense.core.domain.PaymentMethod;
import com.expense.core.domain.PaymentMethodType;
import com.expense.core.report.MonthlySummary;
import com.expense.core.service.ExpenseManager;
import com.expense.core.util.Money;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Currency;

/**
 * Exercises the android.database.sqlite adapters end-to-end on the JVM via
 * Robolectric (real SQLite, no emulator). Confirms CRUD, month queries, the
 * full analytics stack through {@link ExpenseManager}, and idempotent budget
 * upsert — i.e. that persistence genuinely works on-device, not just compiles.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = {34}, manifest = Config.NONE)
public class AndroidPersistenceTest {

    private final Currency myr = Currency.getInstance("MYR");
    private AndroidDatabase db;

    @Before
    public void setUp() {
        Context ctx = RuntimeEnvironment.getApplication();
        File file = ctx.getDatabasePath("test-" + System.nanoTime() + ".db");
        //noinspection ResultOfMethodCallIgnored
        file.getParentFile().mkdirs();
        if (file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
        db = AndroidDatabase.open(file.getAbsolutePath());
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void persistsAndSummarisesAcrossTheFullStack() {
        AndroidAccountRepository accounts = new AndroidAccountRepository(db);
        AndroidCategoryRepository categories = new AndroidCategoryRepository(db);
        AndroidPaymentMethodRepository payments = new AndroidPaymentMethodRepository(db);
        AndroidExpenseRepository expenses = new AndroidExpenseRepository(db);
        AndroidIncomeRepository incomes = new AndroidIncomeRepository(db);
        AndroidBudgetRepository budgets = new AndroidBudgetRepository(db);
        AndroidMonthlySummaryRepository summaries = new AndroidMonthlySummaryRepository(db);

        Account acct = accounts.save(Account.create("Cash", AccountType.CASH, Money.zero(myr)));
        assertNotNull(acct.id());
        Category groceries = categories.save(Category.create("Groceries", CategoryType.EXPENSE, null, null));
        Category salary = categories.save(Category.create("Salary", CategoryType.INCOME, null, null));
        PaymentMethod cash = payments.save(PaymentMethod.create("Cash", PaymentMethodType.CASH));

        // findById round-trips
        assertEquals("Cash", accounts.findById(acct.id()).orElseThrow().name());
        assertEquals(1, categories.findByType(CategoryType.EXPENSE).size());

        LocalDate today = LocalDate.now();
        YearMonth month = YearMonth.now();
        expenses.save(Expense.create(acct.id(), groceries.id(), cash.id(),
                Money.of("120.00", myr), "weekly groceries", today));
        incomes.save(Income.create(acct.id(), salary.id(), Money.of("3000.00", myr), "salary", today));

        assertEquals(1, expenses.findByMonth(month).size());
        assertEquals(1, incomes.findByMonth(month).size());

        // Full analytics via the shared core, backed entirely by the Android adapters.
        ExpenseManager mgr = new ExpenseManager(categories, payments, accounts,
                expenses, incomes, budgets, summaries, myr, null);
        MonthlySummary summary = mgr.summaries().summarize(month);
        assertEquals(0, summary.totalIncome().amount().compareTo(new BigDecimal("3000.00")));
        assertEquals("MYR -120.00", summary.totalExpense().toString());
        assertEquals("MYR 2880.00", summary.netBalance().toString());

        // Budget upsert must replace, not duplicate.
        budgets.save(Budget.create(groceries.id(), month, Money.of("500", myr)));
        budgets.save(Budget.create(groceries.id(), month, Money.of("600", myr)));
        assertEquals(1, budgets.findByMonth(month).size());
        assertEquals("MYR 600.00", budgets.findByMonth(month).get(0).limit().toString());

        // Budget utilisation flows through the summary.
        MonthlySummary withBudget = mgr.summaries().summarize(month);
        assertEquals(1, withBudget.budgetUtilization().size());
        assertTrue(withBudget.budgetUtilization().get(0).utilizationPct() > 0);

        // Snapshot cache upsert is idempotent too.
        mgr.summaries().summarizeAndCache(month);
        mgr.summaries().summarizeAndCache(month);
        assertEquals(month, summaries.findByMonth(month).orElseThrow().month());

        // Delete removes the row.
        long expenseId = expenses.findByMonth(month).get(0).id();
        expenses.deleteById(expenseId);
        assertEquals(0, expenses.findByMonth(month).size());
    }
}
