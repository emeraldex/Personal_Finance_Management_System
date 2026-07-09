package com.expense.core.report;

import com.expense.core.util.Money;

/**
 * Budget-versus-actual for one category in a month.
 *
 * @param categoryId     the capped category
 * @param categoryName   display name
 * @param limit          the budget cap (>= 0)
 * @param spent          actual spend magnitude (>= 0)
 * @param remaining      {@code limit - spent} (may be negative when over budget)
 * @param utilizationPct {@code spent / limit * 100}, or 0 when limit is 0
 * @param overBudget     {@code true} when spend exceeds the cap
 */
public record BudgetUtilization(long categoryId, String categoryName, Money limit,
                                Money spent, Money remaining, double utilizationPct,
                                boolean overBudget) {
}
