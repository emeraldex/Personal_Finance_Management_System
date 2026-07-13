package com.expense.android.data;

import com.expense.core.domain.Income;
import com.expense.core.repository.IncomeRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/** {@code android.database.sqlite} implementation of {@link IncomeRepository}. */
public final class AndroidIncomeRepository extends AndroidSqlSupport implements IncomeRepository {

    public AndroidIncomeRepository(AndroidDatabase database) {
        super(database);
    }

    @Override
    public Income save(Income i) {
        long id = insert(
                "INSERT INTO income(account_id, category_id, amount_minor, currency, description, "
                        + "txn_date, created_at) VALUES (?,?,?,?,?,?,?)",
                i.accountId(), i.categoryId(), i.amount().toMinor(),
                i.amount().currency().getCurrencyCode(), i.description(),
                i.date().toString(), i.createdAt().toString());
        return i.withId(id);
    }

    @Override
    public void update(Income i) {
        execute("UPDATE income SET account_id=?, category_id=?, amount_minor=?, currency=?, "
                        + "description=?, txn_date=? WHERE id=?",
                i.accountId(), i.categoryId(), i.amount().toMinor(),
                i.amount().currency().getCurrencyCode(), i.description(), i.date().toString(), i.id());
    }

    @Override
    public Optional<Income> findById(long id) {
        return queryOne("SELECT * FROM income WHERE id=?", args(id), AndroidMappers::income);
    }

    @Override
    public List<Income> findAll() {
        return query("SELECT * FROM income ORDER BY txn_date DESC, id DESC", null, AndroidMappers::income);
    }

    @Override
    public List<Income> findByDateRange(LocalDate from, LocalDate to) {
        return query("SELECT * FROM income WHERE txn_date BETWEEN ? AND ? ORDER BY txn_date DESC, id DESC",
                args(from.toString(), to.toString()), AndroidMappers::income);
    }

    @Override
    public List<Income> findByMonth(YearMonth month) {
        return findByDateRange(month.atDay(1), month.atEndOfMonth());
    }

    @Override
    public void deleteById(long id) {
        execute("DELETE FROM income WHERE id=?", id);
    }
}
