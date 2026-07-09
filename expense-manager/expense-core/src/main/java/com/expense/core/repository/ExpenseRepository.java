package com.expense.core.repository;

import com.expense.core.domain.Expense;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/** Persistence port for {@link Expense} transactions. */
public interface ExpenseRepository {
    Expense save(Expense expense);

    void update(Expense expense);

    Optional<Expense> findById(long id);

    List<Expense> findAll();

    /** @return expenses with a date in {@code [from, to]} inclusive, newest first. */
    List<Expense> findByDateRange(LocalDate from, LocalDate to);

    /** @return all expenses falling within {@code month}. */
    List<Expense> findByMonth(YearMonth month);

    void deleteById(long id);
}
