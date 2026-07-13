package com.expense.android.data;

import com.expense.core.domain.Expense;
import com.expense.core.repository.ExpenseRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/** {@code android.database.sqlite} implementation of {@link ExpenseRepository}. */
public final class AndroidExpenseRepository extends AndroidSqlSupport implements ExpenseRepository {

    public AndroidExpenseRepository(AndroidDatabase database) {
        super(database);
    }

    @Override
    public Expense save(Expense e) {
        long id = insert(
                "INSERT INTO expense(account_id, category_id, payment_method_id, amount_minor, "
                        + "currency, description, txn_date, created_at) VALUES (?,?,?,?,?,?,?,?)",
                e.accountId(), e.categoryId(), e.paymentMethodId(), e.amount().toMinor(),
                e.amount().currency().getCurrencyCode(), e.description(),
                e.date().toString(), e.createdAt().toString());
        return e.withId(id);
    }

    @Override
    public void update(Expense e) {
        execute("UPDATE expense SET account_id=?, category_id=?, payment_method_id=?, amount_minor=?, "
                        + "currency=?, description=?, txn_date=? WHERE id=?",
                e.accountId(), e.categoryId(), e.paymentMethodId(), e.amount().toMinor(),
                e.amount().currency().getCurrencyCode(), e.description(), e.date().toString(), e.id());
    }

    @Override
    public Optional<Expense> findById(long id) {
        return queryOne("SELECT * FROM expense WHERE id=?", args(id), AndroidMappers::expense);
    }

    @Override
    public List<Expense> findAll() {
        return query("SELECT * FROM expense ORDER BY txn_date DESC, id DESC", null, AndroidMappers::expense);
    }

    @Override
    public List<Expense> findByDateRange(LocalDate from, LocalDate to) {
        return query("SELECT * FROM expense WHERE txn_date BETWEEN ? AND ? ORDER BY txn_date DESC, id DESC",
                args(from.toString(), to.toString()), AndroidMappers::expense);
    }

    @Override
    public List<Expense> findByMonth(YearMonth month) {
        return findByDateRange(month.atDay(1), month.atEndOfMonth());
    }

    @Override
    public void deleteById(long id) {
        execute("DELETE FROM expense WHERE id=?", id);
    }
}
