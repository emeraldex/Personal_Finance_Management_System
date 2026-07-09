package com.expense.core.service;

import com.expense.core.domain.Budget;
import com.expense.core.domain.Category;
import com.expense.core.domain.CategoryType;
import com.expense.core.dto.CreateBudgetRequest;
import com.expense.core.repository.BudgetRepository;
import com.expense.core.repository.CategoryRepository;
import com.expense.core.validation.ValidationErrors;

import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

/**
 * Application service for setting per-category monthly budgets. A budget may
 * only be attached to an existing {@link CategoryType#EXPENSE} category.
 */
public final class BudgetService {

    private final BudgetRepository budgets;
    private final CategoryRepository categories;

    public BudgetService(BudgetRepository budgets, CategoryRepository categories) {
        this.budgets = Objects.requireNonNull(budgets);
        this.categories = Objects.requireNonNull(categories);
    }

    /** Sets (creates or replaces) the budget for a category in a month. */
    public Budget set(CreateBudgetRequest r) {
        ValidationErrors errors = new ValidationErrors();
        errors.addIf(r.month() == null, "month", "Month is required");
        errors.addIf(r.limit() == null, "limit", "Limit is required");
        errors.addIf(r.limit() != null && r.limit().isNegative(), "limit", "Limit must be non-negative");

        Category category = categories.findById(r.categoryId()).orElse(null);
        if (category == null) {
            errors.add("categoryId", "Category does not exist: " + r.categoryId());
        } else if (category.type() != CategoryType.EXPENSE) {
            errors.add("categoryId", "Budgets apply only to EXPENSE categories");
        }
        errors.throwIfInvalid();

        return budgets.save(Budget.create(r.categoryId(), r.month(), r.limit()));
    }

    public List<Budget> listForMonth(YearMonth month) {
        return budgets.findByMonth(month);
    }

    public void delete(long id) {
        budgets.deleteById(id);
    }
}
