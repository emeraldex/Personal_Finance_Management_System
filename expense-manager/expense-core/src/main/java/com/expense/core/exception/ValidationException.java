package com.expense.core.exception;

import java.util.List;
import java.util.Map;

/**
 * Aggregates one or more field-level validation failures. UIs can bind
 * {@link #errors()} directly to form fields.
 */
public final class ValidationException extends ExpenseException {

    private static final long serialVersionUID = 1L;

    private final transient Map<String, List<String>> errors;

    public ValidationException(Map<String, List<String>> errors) {
        super("Validation failed: " + errors);
        this.errors = Map.copyOf(errors);
    }

    /** @return an immutable map of field name to the messages that failed for it. */
    public Map<String, List<String>> errors() {
        return errors;
    }
}
