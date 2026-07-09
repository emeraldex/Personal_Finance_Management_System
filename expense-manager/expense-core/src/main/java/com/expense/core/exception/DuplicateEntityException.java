package com.expense.core.exception;

/** Thrown when a uniqueness constraint (e.g. category name) would be violated. */
public final class DuplicateEntityException extends ExpenseException {

    private static final long serialVersionUID = 1L;

    public DuplicateEntityException(String message) {
        super(message);
    }
}
