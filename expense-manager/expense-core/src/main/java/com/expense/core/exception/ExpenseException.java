package com.expense.core.exception;

/**
 * Base type for all checked-at-runtime domain failures raised by the core.
 * Callers (UI layers) can catch this single type to present errors uniformly.
 */
public abstract class ExpenseException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    protected ExpenseException(String message) {
        super(message);
    }

    protected ExpenseException(String message, Throwable cause) {
        super(message, cause);
    }
}
