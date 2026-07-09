package com.expense.core.service;

import com.expense.core.domain.Category;
import com.expense.core.domain.CategoryType;
import com.expense.core.domain.Expense;
import com.expense.core.dto.CreateExpenseRequest;
import com.expense.core.dto.UpdateExpenseRequest;
import com.expense.core.exception.EntityNotFoundException;
import com.expense.core.repository.AccountRepository;
import com.expense.core.repository.CategoryRepository;
import com.expense.core.repository.ExpenseRepository;
import com.expense.core.repository.PaymentMethodRepository;
import com.expense.core.util.Money;
import com.expense.core.validation.ValidationErrors;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

/**
 * Application service implementing the expense side of the domain. Enforces the
 * business rule that expenses are stored as negative amounts, validates foreign
 * keys, and ensures any supplied category is of type {@link CategoryType#EXPENSE}.
 */
public final class ExpenseService {

    private final ExpenseRepository expenses;
    private final AccountRepository accounts;
    private final CategoryRepository categories;
    private final PaymentMethodRepository paymentMethods;

    public ExpenseService(ExpenseRepository expenses, AccountRepository accounts,
                          CategoryRepository categories, PaymentMethodRepository paymentMethods) {
        this.expenses = Objects.requireNonNull(expenses);
        this.accounts = Objects.requireNonNull(accounts);
        this.categories = Objects.requireNonNull(categories);
        this.paymentMethods = Objects.requireNonNull(paymentMethods);
    }

    /** Records a new expense; the amount magnitude is normalised to a negative stored value. */
    public Expense create(CreateExpenseRequest r) {
        validate(r.accountId(), r.categoryId(), r.paymentMethodId(), r.amount(), r.date());
        Expense expense = Expense.create(
                r.accountId(), r.categoryId(), r.paymentMethodId(),
                r.amount(), safeDescription(r.description()), r.date());
        return expenses.save(expense);
    }

    /** Updates an existing expense in place. */
    public Expense update(UpdateExpenseRequest r) {
        Expense existing = require(r.id());
        validate(r.accountId(), r.categoryId(), r.paymentMethodId(), r.amount(), r.date());
        Expense updated = new Expense(existing.id(), r.accountId(), r.categoryId(),
                r.paymentMethodId(), r.amount().abs().negate(),
                safeDescription(r.description()), r.date(), existing.createdAt());
        expenses.update(updated);
        return updated;
    }

    public void delete(long id) {
        require(id);
        expenses.deleteById(id);
    }

    public Expense get(long id) {
        return require(id);
    }

    public List<Expense> listAll() {
        return expenses.findAll();
    }

    public List<Expense> listByMonth(YearMonth month) {
        return expenses.findByMonth(month);
    }

    public List<Expense> listByDateRange(LocalDate from, LocalDate to) {
        return expenses.findByDateRange(from, to);
    }

    private void validate(long accountId, Long categoryId, Long paymentMethodId,
                          Money amount, LocalDate date) {
        ValidationErrors errors = new ValidationErrors();
        errors.addIf(amount == null, "amount", "Amount is required");
        errors.addIf(amount != null && amount.isZero(), "amount", "Amount must be non-zero");
        errors.addIf(date == null, "date", "Date is required");
        errors.addIf(accounts.findById(accountId).isEmpty(), "accountId",
                "Account does not exist: " + accountId);

        if (categoryId != null) {
            Category category = categories.findById(categoryId).orElse(null);
            if (category == null) {
                errors.add("categoryId", "Category does not exist: " + categoryId);
            } else if (category.type() != CategoryType.EXPENSE) {
                errors.add("categoryId", "Category must be of type EXPENSE");
            }
        }
        if (paymentMethodId != null && paymentMethods.findById(paymentMethodId).isEmpty()) {
            errors.add("paymentMethodId", "Payment method does not exist: " + paymentMethodId);
        }
        errors.throwIfInvalid();
    }

    private Expense require(long id) {
        return expenses.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Expense", id));
    }

    private static String safeDescription(String description) {
        return description == null ? "" : description.strip();
    }
}
