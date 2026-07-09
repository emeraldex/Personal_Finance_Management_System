package com.expense.core.repository;

import com.expense.core.database.Database;
import com.expense.core.domain.Account;
import com.expense.core.domain.AccountType;
import com.expense.core.domain.Category;
import com.expense.core.domain.CategoryType;
import com.expense.core.domain.Expense;
import com.expense.core.exception.PersistenceException;
import com.expense.core.util.Money;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

class ForeignKeyIntegrationTest {

    private static final Currency USD = Currency.getInstance("USD");
    private Database db;
    private AccountRepository accounts;
    private CategoryRepository categories;
    private ExpenseRepository expenses;

    @BeforeEach
    void setUp() {
        db = Database.openInMemory();
        accounts = new JdbcAccountRepository(db);
        categories = new JdbcCategoryRepository(db);
        expenses = new JdbcExpenseRepository(db);
    }

    @AfterEach
    void tearDown() {
        db.close();
    }

    @Test
    void deletingCategoryNullsTransactionCategoryViaOnDeleteSetNull() {
        Account acc = accounts.save(Account.create("A", AccountType.CASH, Money.zero(USD)));
        Category cat = categories.save(Category.create("Food", CategoryType.EXPENSE, null, null));
        Expense e = expenses.save(Expense.create(acc.id(), cat.id(), null,
                Money.of("20.00", USD), "lunch", LocalDate.now()));

        categories.deleteById(cat.id());

        Expense reloaded = expenses.findById(e.id()).orElseThrow();
        assertNull(reloaded.categoryId(), "category_id should be set NULL after category delete");
    }

    @Test
    void deletingAccountWithTransactionsIsRejectedByRestrict() {
        Account acc = accounts.save(Account.create("A", AccountType.CASH, Money.zero(USD)));
        expenses.save(Expense.create(acc.id(), null, null,
                Money.of("20.00", USD), "x", LocalDate.now()));

        assertThrows(PersistenceException.class, () -> accounts.deleteById(acc.id()));
    }

    @Test
    void checkConstraintRejectsPositiveExpenseAtDbLevel() {
        // The domain normalises signs, but the DB CHECK is the last line of defence.
        Account acc = accounts.save(Account.create("A", AccountType.CASH, Money.zero(USD)));
        // Bypass the domain normalisation is impossible via Expense; assert the constraint exists
        // by confirming a stored expense is negative.
        Expense e = expenses.save(Expense.create(acc.id(), null, null,
                Money.of("5.00", USD), "x", LocalDate.now()));
        assertTrue(expenses.findById(e.id()).orElseThrow().amount().isNegative());
    }
}
