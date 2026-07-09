package com.expense.core.repository;

import com.expense.core.domain.Income;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/** Persistence port for {@link Income} transactions. */
public interface IncomeRepository {
    Income save(Income income);

    void update(Income income);

    Optional<Income> findById(long id);

    List<Income> findAll();

    List<Income> findByDateRange(LocalDate from, LocalDate to);

    List<Income> findByMonth(YearMonth month);

    void deleteById(long id);
}
