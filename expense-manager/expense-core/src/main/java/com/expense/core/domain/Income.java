package com.expense.core.domain;

import com.expense.core.util.Money;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Immutable income transaction. Per the project's business rules the stored
 * {@link #amount} is always non-negative (money entering an account). The
 * constructor normalises the sign so callers cannot accidentally persist a
 * negative income.
 *
 * @param id         persistence id, {@code null} before insertion
 * @param accountId  owning account
 * @param categoryId category id ({@link CategoryType#INCOME}) or {@code null}
 * @param amount     non-negative amount (money in)
 * @param description free text, never {@code null}
 * @param date       transaction date
 * @param createdAt  audit timestamp of insertion
 */
public record Income(Long id, long accountId, Long categoryId,
                     Money amount, String description, LocalDate date, Instant createdAt)
        implements Transaction {

    public Income {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(date, "date");
        if (amount.isNegative()) {
            amount = amount.abs();
        }
    }

    public static Income create(long accountId, Long categoryId,
                                Money magnitude, String description, LocalDate date) {
        return new Income(null, accountId, categoryId,
                magnitude.abs(), description, date, Instant.now());
    }

    public Income withId(long newId) {
        return new Income(newId, accountId, categoryId, amount, description, date, createdAt);
    }

    @Override
    public TransactionType type() {
        return TransactionType.INCOME;
    }

    @Override
    public Money signedAmount() {
        return amount;
    }
}
