package com.expense.core.dto;

import com.expense.core.util.Money;

import java.time.LocalDate;

/**
 * Command to create an expense. {@code amount} is supplied as a positive
 * magnitude; the service normalises it to a negative stored value.
 */
public record CreateExpenseRequest(long accountId, Long categoryId, Long paymentMethodId,
                                   Money amount, String description, LocalDate date) {
}
