package com.expense.desktop;

import com.expense.core.domain.AccountType;
import com.expense.core.domain.CategoryType;
import com.expense.core.domain.PaymentMethodType;
import com.expense.core.dto.CreateAccountRequest;
import com.expense.core.dto.CreateCategoryRequest;
import com.expense.core.dto.CreatePaymentMethodRequest;
import com.expense.core.exception.ExpenseException;
import com.expense.core.service.ExpenseManager;
import com.expense.core.util.Money;

import java.util.Currency;

/**
 * Populates a brand-new database with a small set of usable defaults so the app
 * is functional on first launch. Each entity family is seeded independently and
 * only when it is empty, so this is safe to run on every startup and never
 * duplicates data a user has already created or deleted.
 */
public final class DefaultData {

    private DefaultData() {
    }

    /** Seeds default accounts, categories and payment methods where none exist yet. */
    public static void seedIfEmpty(ExpenseManager manager, Currency currency) {
        seedAccounts(manager, currency);
        seedCategories(manager);
        seedPaymentMethods(manager);
    }

    private static void seedAccounts(ExpenseManager manager, Currency currency) {
        if (!manager.accounts().list().isEmpty()) {
            return;
        }
        Money zero = Money.zero(currency);
        create(() -> manager.accounts().create(new CreateAccountRequest("Cash", AccountType.CASH, zero)));
        create(() -> manager.accounts().create(new CreateAccountRequest("Checking", AccountType.CHECKING, zero)));
        create(() -> manager.accounts().create(new CreateAccountRequest("Savings", AccountType.SAVINGS, zero)));
    }

    private static void seedCategories(ExpenseManager manager) {
        if (!manager.categories().list().isEmpty()) {
            return;
        }
        String[] expenses = {
                "Groceries", "Rent", "Utilities", "Dining", "Transport",
                "Entertainment", "Health", "Shopping", "Other"
        };
        for (String name : expenses) {
            create(() -> manager.categories().create(
                    new CreateCategoryRequest(name, CategoryType.EXPENSE, null, null)));
        }
        String[] incomes = {"Salary", "Bonus", "Interest", "Other Income"};
        for (String name : incomes) {
            create(() -> manager.categories().create(
                    new CreateCategoryRequest(name, CategoryType.INCOME, null, null)));
        }
    }

    private static void seedPaymentMethods(ExpenseManager manager) {
        if (!manager.paymentMethods().list().isEmpty()) {
            return;
        }
        create(() -> manager.paymentMethods().create(
                new CreatePaymentMethodRequest("Cash", PaymentMethodType.CASH)));
        create(() -> manager.paymentMethods().create(
                new CreatePaymentMethodRequest("Debit Card", PaymentMethodType.DEBIT_CARD)));
        create(() -> manager.paymentMethods().create(
                new CreatePaymentMethodRequest("Credit Card", PaymentMethodType.CREDIT_CARD)));
        create(() -> manager.paymentMethods().create(
                new CreatePaymentMethodRequest("Bank Transfer", PaymentMethodType.BANK_TRANSFER)));
    }

    /** Runs one seed insert, ignoring duplicates so partial seeds are harmless. */
    private static void create(Runnable insert) {
        try {
            insert.run();
        } catch (ExpenseException ignored) {
            // Already present (e.g. a partially seeded DB) — leave the existing row untouched.
        }
    }
}
