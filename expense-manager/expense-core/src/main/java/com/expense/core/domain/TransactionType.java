package com.expense.core.domain;

/** Marks the direction of a {@link Transaction}. */
public enum TransactionType {
    /** Money leaving an account. Stored as a negative amount. */
    EXPENSE,
    /** Money entering an account. Stored as a positive amount. */
    INCOME
}
