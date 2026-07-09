package com.expense.core.repository;

import com.expense.core.database.ConnectionProvider;
import com.expense.core.domain.Budget;
import com.expense.core.mapper.Mappers;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/** SQLite/JDBC implementation of {@link BudgetRepository} with upsert semantics. */
public final class JdbcBudgetRepository extends JdbcSupport implements BudgetRepository {

    public JdbcBudgetRepository(ConnectionProvider connections) {
        super(connections);
    }

    @Override
    public Budget save(Budget b) {
        // UPSERT on the (category_id, month) unique key.
        long id = insertReturning(
                "INSERT INTO budget(category_id, month, limit_minor, currency) VALUES (?,?,?,?) "
                        + "ON CONFLICT(category_id, month) DO UPDATE SET "
                        + "limit_minor=excluded.limit_minor, currency=excluded.currency "
                        + "RETURNING id",
                ps -> {
                    ps.setLong(1, b.categoryId());
                    ps.setString(2, b.month().toString());
                    ps.setLong(3, b.limit().toMinor());
                    ps.setString(4, b.limit().currency().getCurrencyCode());
                });
        return b.withId(id);
    }

    @Override
    public Optional<Budget> findByCategoryAndMonth(long categoryId, YearMonth month) {
        return queryOne("SELECT * FROM budget WHERE category_id=? AND month=?",
                ps -> {
                    ps.setLong(1, categoryId);
                    ps.setString(2, month.toString());
                }, Mappers::budget);
    }

    @Override
    public List<Budget> findByMonth(YearMonth month) {
        return query("SELECT * FROM budget WHERE month=?",
                ps -> ps.setString(1, month.toString()), Mappers::budget);
    }

    @Override
    public void deleteById(long id) {
        execute("DELETE FROM budget WHERE id=?", ps -> ps.setLong(1, id));
    }
}
