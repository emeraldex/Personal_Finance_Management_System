package com.expense.core.util;

import com.expense.core.domain.Category;
import com.expense.core.domain.CategoryType;
import com.expense.core.network.CategorySuggestion;
import com.expense.core.network.HeuristicExpenseCategorizer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HeuristicExpenseCategorizerTest {

    private final HeuristicExpenseCategorizer categorizer = new HeuristicExpenseCategorizer();

    private final List<Category> categories = List.of(
            new Category(1L, "Groceries", CategoryType.EXPENSE, null, null, false),
            new Category(2L, "Transport", CategoryType.EXPENSE, null, null, false),
            new Category(3L, "Rent", CategoryType.EXPENSE, null, null, false));

    @Test
    void matchesCategoryByKeyword() {
        Optional<CategorySuggestion> s = categorizer.suggest("Monthly rent", categories);
        assertTrue(s.isPresent());
        assertEquals(3L, s.get().categoryId());
    }

    @Test
    void returnsEmptyWhenNothingMatches() {
        assertTrue(categorizer.suggest("Cinema tickets", categories).isEmpty());
    }

    @Test
    void returnsEmptyForBlankDescription() {
        assertTrue(categorizer.suggest("   ", categories).isEmpty());
    }
}
