package com.expense.core.report;

import com.expense.core.util.Money;

/**
 * One row of a category breakdown.
 *
 * @param categoryId   category id ({@code null} for the "Uncategorised" bucket)
 * @param categoryName display name
 * @param total        signed total for the category over the period
 * @param percentage   share of the relevant total, 0..100
 */
public record CategoryBreakdownItem(Long categoryId, String categoryName,
                                    Money total, double percentage) {
}
