package com.expense.core.network;

import com.expense.core.domain.Category;

import java.util.List;
import java.util.Optional;

/**
 * Seam for automatic expense categorisation. The default implementation is a
 * local, offline heuristic; a future ML/LLM-backed implementation can replace it
 * behind this same interface, enabling "AI-powered categorisation" without any
 * architectural change.
 */
public interface ExpenseCategorizer {
    /**
     * Suggests the best category for a transaction description.
     *
     * @param description  free-text description
     * @param candidates   the categories to choose from
     * @return the best suggestion, or empty if none is confident enough
     */
    Optional<CategorySuggestion> suggest(String description, List<Category> candidates);
}
