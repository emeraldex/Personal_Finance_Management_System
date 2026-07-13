package com.expense.android.data;

import com.expense.core.domain.Budget;
import com.expense.core.repository.BudgetRepository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * {@code android.database.sqlite} implementation of {@link BudgetRepository}.
 *
 * <p>The upsert is done as an explicit read-then-write rather than SQLite's
 * {@code INSERT ... ON CONFLICT ... RETURNING}, because that syntax requires
 * SQLite 3.35+ which is not present on older Android API levels. The behaviour —
 * one budget per (category, month) — is identical.</p>
 */
public final class AndroidBudgetRepository extends AndroidSqlSupport implements BudgetRepository {

    public AndroidBudgetRepository(AndroidDatabase database) {
        super(database);
    }

    @Override
    public Budget save(Budget b) {
        Optional<Budget> existing = findByCategoryAndMonth(b.categoryId(), b.month());
        long id;
        if (existing.isPresent()) {
            id = existing.get().id();
            execute("UPDATE budget SET limit_minor=?, currency=? WHERE id=?",
                    b.limit().toMinor(), b.limit().currency().getCurrencyCode(), id);
        } else {
            id = insert("INSERT INTO budget(category_id, month, limit_minor, currency) VALUES (?,?,?,?)",
                    b.categoryId(), b.month().toString(), b.limit().toMinor(),
                    b.limit().currency().getCurrencyCode());
        }
        return b.withId(id);
    }

    @Override
    public Optional<Budget> findByCategoryAndMonth(long categoryId, YearMonth month) {
        return queryOne("SELECT * FROM budget WHERE category_id=? AND month=?",
                args(categoryId, month.toString()), AndroidMappers::budget);
    }

    @Override
    public List<Budget> findByMonth(YearMonth month) {
        return query("SELECT * FROM budget WHERE month=?", args(month.toString()), AndroidMappers::budget);
    }

    @Override
    public void deleteById(long id) {
        execute("DELETE FROM budget WHERE id=?", id);
    }
}
