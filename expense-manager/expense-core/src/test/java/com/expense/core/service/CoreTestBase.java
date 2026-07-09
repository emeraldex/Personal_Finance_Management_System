package com.expense.core.service;

import com.expense.core.database.Database;
import com.expense.core.domain.Account;
import com.expense.core.domain.AccountType;
import com.expense.core.domain.Category;
import com.expense.core.domain.CategoryType;
import com.expense.core.domain.PaymentMethod;
import com.expense.core.domain.PaymentMethodType;
import com.expense.core.dto.CreateAccountRequest;
import com.expense.core.dto.CreateCategoryRequest;
import com.expense.core.dto.CreatePaymentMethodRequest;
import com.expense.core.util.Money;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.Currency;

/**
 * Shared fixture: an in-memory database wired through {@link ExpenseManager},
 * seeded with a default account, expense/income categories and a payment method.
 */
abstract class CoreTestBase {

    protected static final Currency USD = Currency.getInstance("USD");

    protected Database database;
    protected ExpenseManager manager;

    protected Account account;
    protected Category groceries;
    protected Category rent;
    protected Category salary;
    protected PaymentMethod cash;

    @BeforeEach
    void setUp() {
        database = Database.openInMemory();
        manager = new ExpenseManager(database, USD);

        account = manager.accounts().create(
                new CreateAccountRequest("Checking", AccountType.CHECKING, Money.zero(USD)));
        groceries = manager.categories().create(
                new CreateCategoryRequest("Groceries", CategoryType.EXPENSE, "#4CAF50", "cart"));
        rent = manager.categories().create(
                new CreateCategoryRequest("Rent", CategoryType.EXPENSE, "#F44336", "home"));
        salary = manager.categories().create(
                new CreateCategoryRequest("Salary", CategoryType.INCOME, "#2196F3", "wallet"));
        cash = manager.paymentMethods().create(
                new CreatePaymentMethodRequest("Cash", PaymentMethodType.CASH));
    }

    @AfterEach
    void tearDown() {
        manager.close();
    }

    protected Money usd(String amount) {
        return Money.of(amount, USD);
    }
}
