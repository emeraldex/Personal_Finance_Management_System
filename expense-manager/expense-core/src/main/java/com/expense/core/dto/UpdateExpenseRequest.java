package com.expense.core.dto;

import com.expense.core.util.Money;

import java.time.LocalDate;

/** Command to update an existing expense identified by {@code id}. */
public record UpdateExpenseRequest(long id, long accountId, Long categoryId, Long paymentMethodId,
                                   Money amount, String description, LocalDate date) {
}
