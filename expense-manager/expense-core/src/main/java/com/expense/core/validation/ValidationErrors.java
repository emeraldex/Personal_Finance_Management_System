package com.expense.core.validation;

import com.expense.core.exception.ValidationException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Mutable accumulator for field-level validation messages. Collect all problems
 * first, then call {@link #throwIfInvalid()} so the caller sees every error at
 * once rather than one at a time.
 */
public final class ValidationErrors {

    private final Map<String, List<String>> errors = new LinkedHashMap<>();

    /** Records {@code message} against {@code field}. */
    public ValidationErrors add(String field, String message) {
        errors.computeIfAbsent(field, k -> new ArrayList<>()).add(message);
        return this;
    }

    /** Records {@code message} against {@code field} only when {@code condition} holds. */
    public ValidationErrors addIf(boolean condition, String field, String message) {
        if (condition) {
            add(field, message);
        }
        return this;
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    /** @throws ValidationException if any error was accumulated. */
    public void throwIfInvalid() {
        if (!isValid()) {
            throw new ValidationException(errors);
        }
    }
}
