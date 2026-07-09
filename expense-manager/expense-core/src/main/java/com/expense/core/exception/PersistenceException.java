package com.expense.core.exception;

/** Wraps low-level {@link java.sql.SQLException}s in the domain exception type. */
public final class PersistenceException extends ExpenseException {

    private static final long serialVersionUID = 1L;

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
