package com.expense.core.exception;

/** Thrown when a lookup by id finds no matching row. */
public final class EntityNotFoundException extends ExpenseException {

    private static final long serialVersionUID = 1L;

    public EntityNotFoundException(String entity, Object id) {
        super(entity + " not found: id=" + id);
    }
}
