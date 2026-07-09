package com.expense.core.validation;

/**
 * Strategy that inspects a target and appends any problems to the supplied
 * {@link ValidationErrors}. Kept as a functional interface so validators compose
 * and can be injected.
 *
 * @param <T> the type being validated
 */
@FunctionalInterface
public interface Validator<T> {
    void validate(T target, ValidationErrors errors);
}
