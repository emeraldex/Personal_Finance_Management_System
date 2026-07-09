package com.expense.core.dto;

import com.expense.core.util.Money;

import java.time.LocalDate;

/** Command to create an income entry. {@code amount} is a positive magnitude. */
public record CreateIncomeRequest(long accountId, Long categoryId,
                                  Money amount, String description, LocalDate date) {
}
