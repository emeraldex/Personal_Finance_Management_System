package com.expense.core.domain;

import com.expense.core.util.Money;

import java.time.YearMonth;
import java.util.Objects;

/**
 * Immutable spending limit for a category in a specific month. A budget's
 * {@link #limit} is stored as a non-negative magnitude (a cap on spend), even
 * though the expenses it governs are negative.
 *
 * @param id         persistence id, {@code null} before insertion
 * @param categoryId the {@link CategoryType#EXPENSE} category being capped
 * @param month      the month the cap applies to
 * @param limit      non-negative spending cap
 */
public record Budget(Long id, long categoryId, YearMonth month, Money limit) {

    public Budget {
        Objects.requireNonNull(month, "month");
        Objects.requireNonNull(limit, "limit");
        if (limit.isNegative()) {
            throw new IllegalArgumentException("Budget limit must be non-negative");
        }
    }

    public static Budget create(long categoryId, YearMonth month, Money limit) {
        return new Budget(null, categoryId, month, limit);
    }

    public Budget withId(long newId) {
        return new Budget(newId, categoryId, month, limit);
    }
}
