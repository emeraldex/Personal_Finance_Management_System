package com.expense.core.domain;

import com.expense.core.util.Money;

import java.time.LocalDate;

/**
 * Common contract for signed financial movements. Implemented by {@link Expense}
 * and {@link Income} so that analytics and reporting can treat both uniformly
 * without duplicating logic.
 *
 * <p>The sign convention is enforced by the concrete types: an {@link Expense}
 * always exposes a non-positive {@link #signedAmount()}, an {@link Income} a
 * non-negative one.</p>
 */
public sealed interface Transaction permits Expense, Income {

    /** @return the database identity, or {@code null} for a not-yet-persisted record. */
    Long id();

    /** @return the owning account id. */
    long accountId();

    /** @return the category id, or {@code null} if uncategorised. */
    Long categoryId();

    /** @return the calendar date on which the transaction occurred. */
    LocalDate date();

    /** @return a free-text description; never {@code null} but may be blank. */
    String description();

    /** @return whether this is an {@link TransactionType#EXPENSE} or {@link TransactionType#INCOME}. */
    TransactionType type();

    /**
     * @return the signed amount: negative for expenses, positive for income.
     *         This is the value used directly in cash-flow and net-balance math.
     */
    Money signedAmount();
}
