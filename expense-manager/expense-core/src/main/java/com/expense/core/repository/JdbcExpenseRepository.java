package com.expense.core.repository;

import com.expense.core.database.ConnectionProvider;
import com.expense.core.domain.Expense;
import com.expense.core.mapper.Mappers;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/** SQLite/JDBC implementation of {@link ExpenseRepository}. */
public final class JdbcExpenseRepository extends JdbcSupport implements ExpenseRepository {

    public JdbcExpenseRepository(ConnectionProvider connections) {
        super(connections);
    }

    private static void setNullableLong(PreparedStatement ps, int idx, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(idx, Types.INTEGER);
        } else {
            ps.setLong(idx, value);
        }
    }

    @Override
    public Expense save(Expense e) {
        long id = insert(
                "INSERT INTO expense(account_id, category_id, payment_method_id, amount_minor, "
                        + "currency, description, txn_date, created_at) VALUES (?,?,?,?,?,?,?,?)",
                ps -> {
                    ps.setLong(1, e.accountId());
                    setNullableLong(ps, 2, e.categoryId());
                    setNullableLong(ps, 3, e.paymentMethodId());
                    ps.setLong(4, e.amount().toMinor());
                    ps.setString(5, e.amount().currency().getCurrencyCode());
                    ps.setString(6, e.description());
                    ps.setString(7, e.date().toString());
                    ps.setString(8, e.createdAt().toString());
                });
        return e.withId(id);
    }

    @Override
    public void update(Expense e) {
        execute("UPDATE expense SET account_id=?, category_id=?, payment_method_id=?, amount_minor=?, "
                        + "currency=?, description=?, txn_date=? WHERE id=?",
                ps -> {
                    ps.setLong(1, e.accountId());
                    setNullableLong(ps, 2, e.categoryId());
                    setNullableLong(ps, 3, e.paymentMethodId());
                    ps.setLong(4, e.amount().toMinor());
                    ps.setString(5, e.amount().currency().getCurrencyCode());
                    ps.setString(6, e.description());
                    ps.setString(7, e.date().toString());
                    ps.setLong(8, e.id());
                });
    }

    @Override
    public Optional<Expense> findById(long id) {
        return queryOne("SELECT * FROM expense WHERE id=?",
                ps -> ps.setLong(1, id), Mappers::expense);
    }

    @Override
    public List<Expense> findAll() {
        return query("SELECT * FROM expense ORDER BY txn_date DESC, id DESC", NO_ARGS, Mappers::expense);
    }

    @Override
    public List<Expense> findByDateRange(LocalDate from, LocalDate to) {
        return query("SELECT * FROM expense WHERE txn_date BETWEEN ? AND ? ORDER BY txn_date DESC, id DESC",
                ps -> {
                    ps.setString(1, from.toString());
                    ps.setString(2, to.toString());
                }, Mappers::expense);
    }

    @Override
    public List<Expense> findByMonth(YearMonth month) {
        return findByDateRange(month.atDay(1), month.atEndOfMonth());
    }

    @Override
    public void deleteById(long id) {
        execute("DELETE FROM expense WHERE id=?", ps -> ps.setLong(1, id));
    }
}
