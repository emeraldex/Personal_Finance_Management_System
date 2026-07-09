package com.expense.core.network;

/**
 * A ranked category suggestion for a transaction.
 *
 * @param categoryId the suggested category id
 * @param confidence 0.0..1.0 confidence score
 */
public record CategorySuggestion(long categoryId, double confidence) {
}
