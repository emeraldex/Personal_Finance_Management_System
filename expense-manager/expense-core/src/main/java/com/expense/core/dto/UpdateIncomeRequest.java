package com.expense.core.dto;

import com.expense.core.util.Money;

import java.time.LocalDate;

/** Command to update an existing income entry identified by {@code id}. */
public record UpdateIncomeRequest(long id, long accountId, Long categoryId,
                                  Money amount, String description, LocalDate date) {
}
