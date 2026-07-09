package com.expense.core.domain;

import com.expense.core.util.Money;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Immutable expense transaction. Per the project's business rules the stored
 * {@link #amount} is always non-positive (money leaving an account). The
 * constructor normalises any supplied amount to be non-positive so callers can
 * pass a natural positive figure or an already-negative one interchangeably.
 *
 * @param id              persistence id, {@code null} before insertion
 * @param accountId       owning account
 * @param categoryId      category id ({@link CategoryType#EXPENSE}) or {@code null}
 * @param paymentMethodId payment method id or {@code null}
 * @param amount          non-positive amount (money out)
 * @param description     free text, never {@code null}
 * @param date            transaction date
 * @param createdAt       audit timestamp of insertion
 */
public record Expense(Long id, long accountId, Long categoryId, Long paymentMethodId,
                      Money amount, String description, LocalDate date, Instant createdAt)
        implements Transaction {

    public Expense {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(date, "date");
        // Enforce sign convention: expenses are stored as non-positive amounts.
        if (amount.isPositive()) {
            amount = amount.negate();
        }
    }

    /**
     * Factory that accepts a natural (positive) magnitude and stores it as a
     * negative expense.
     *
     * @param magnitude the positive spend amount; its sign is normalised
     */
    public static Expense create(long accountId, Long categoryId, Long paymentMethodId,
                                 Money magnitude, String description, LocalDate date) {
        return new Expense(null, accountId, categoryId, paymentMethodId,
                magnitude.abs().negate(), description, date, Instant.now());
    }

    public Expense withId(long newId) {
        return new Expense(newId, accountId, categoryId, paymentMethodId,
                amount, description, date, createdAt);
    }

    @Override
    public TransactionType type() {
        return TransactionType.EXPENSE;
    }

    @Override
    public Money signedAmount() {
        return amount;
    }
}
