package com.expense.core.service;

import com.expense.core.domain.Category;
import com.expense.core.domain.CategoryType;
import com.expense.core.domain.Income;
import com.expense.core.dto.CreateIncomeRequest;
import com.expense.core.dto.UpdateIncomeRequest;
import com.expense.core.exception.EntityNotFoundException;
import com.expense.core.repository.AccountRepository;
import com.expense.core.repository.CategoryRepository;
import com.expense.core.repository.IncomeRepository;
import com.expense.core.util.Money;
import com.expense.core.validation.ValidationErrors;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

/**
 * Application service implementing the income side of the domain. Enforces the
 * business rule that income is stored as positive amounts and that any supplied
 * category is of type {@link CategoryType#INCOME}.
 */
public final class IncomeService {

    private final IncomeRepository incomes;
    private final AccountRepository accounts;
    private final CategoryRepository categories;

    public IncomeService(IncomeRepository incomes, AccountRepository accounts,
                         CategoryRepository categories) {
        this.incomes = Objects.requireNonNull(incomes);
        this.accounts = Objects.requireNonNull(accounts);
        this.categories = Objects.requireNonNull(categories);
    }

    public Income create(CreateIncomeRequest r) {
        validate(r.accountId(), r.categoryId(), r.amount(), r.date());
        Income income = Income.create(
                r.accountId(), r.categoryId(), r.amount(),
                safeDescription(r.description()), r.date());
        return incomes.save(income);
    }

    public Income update(UpdateIncomeRequest r) {
        Income existing = require(r.id());
        validate(r.accountId(), r.categoryId(), r.amount(), r.date());
        Income updated = new Income(existing.id(), r.accountId(), r.categoryId(),
                r.amount().abs(), safeDescription(r.description()), r.date(), existing.createdAt());
        incomes.update(updated);
        return updated;
    }

    public void delete(long id) {
        require(id);
        incomes.deleteById(id);
    }

    public Income get(long id) {
        return require(id);
    }

    public List<Income> listAll() {
        return incomes.findAll();
    }

    public List<Income> listByMonth(YearMonth month) {
        return incomes.findByMonth(month);
    }

    public List<Income> listByDateRange(LocalDate from, LocalDate to) {
        return incomes.findByDateRange(from, to);
    }

    private void validate(long accountId, Long categoryId, Money amount, LocalDate date) {
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
            } else if (category.type() != CategoryType.INCOME) {
                errors.add("categoryId", "Category must be of type INCOME");
            }
        }
        errors.throwIfInvalid();
    }

    private Income require(long id) {
        return incomes.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Income", id));
    }

    private static String safeDescription(String description) {
        return description == null ? "" : description.strip();
    }
}
