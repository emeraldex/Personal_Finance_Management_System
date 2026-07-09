package com.expense.core.repository;

import com.expense.core.domain.Budget;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/** Persistence port for {@link Budget} caps. */
public interface BudgetRepository {
    /** Inserts or replaces the budget for a (category, month) pair. */
    Budget save(Budget budget);

    Optional<Budget> findByCategoryAndMonth(long categoryId, YearMonth month);

    List<Budget> findByMonth(YearMonth month);

    void deleteById(long id);
}
