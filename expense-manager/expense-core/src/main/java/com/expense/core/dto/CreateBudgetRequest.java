package com.expense.core.dto;

import com.expense.core.util.Money;

import java.time.YearMonth;

/** Command to set (or replace) a category budget for a month. */
public record CreateBudgetRequest(long categoryId, YearMonth month, Money limit) {
}
