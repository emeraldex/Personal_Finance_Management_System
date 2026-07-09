package com.expense.core.report;

import com.expense.core.util.Money;

import java.time.LocalDate;

/**
 * Daily cash-flow data point used for charts.
 *
 * @param date    the day
 * @param income  income on the day (>= 0)
 * @param expense expenses on the day (<= 0)
 * @param net     income + expense
 */
public record CashFlowPoint(LocalDate date, Money income, Money expense, Money net) {
}
