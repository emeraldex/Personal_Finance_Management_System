package com.expense.core.repository;

import com.expense.core.database.ConnectionProvider;
import com.expense.core.domain.Income;
import com.expense.core.mapper.Mappers;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/** SQLite/JDBC implementation of {@link IncomeRepository}. */
public final class JdbcIncomeRepository extends JdbcSupport implements IncomeRepository {

    public JdbcIncomeRepository(ConnectionProvider connections) {
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
    public Income save(Income i) {
        long id = insert(
                "INSERT INTO income(account_id, category_id, amount_minor, currency, description, "
                        + "txn_date, created_at) VALUES (?,?,?,?,?,?,?)",
                ps -> {
                    ps.setLong(1, i.accountId());
                    setNullableLong(ps, 2, i.categoryId());
                    ps.setLong(3, i.amount().toMinor());
                    ps.setString(4, i.amount().currency().getCurrencyCode());
                    ps.setString(5, i.description());
                    ps.setString(6, i.date().toString());
                    ps.setString(7, i.createdAt().toString());
                });
        return i.withId(id);
    }

    @Override
    public void update(Income i) {
        execute("UPDATE income SET account_id=?, category_id=?, amount_minor=?, currency=?, "
                        + "description=?, txn_date=? WHERE id=?",
                ps -> {
                    ps.setLong(1, i.accountId());
                    setNullableLong(ps, 2, i.categoryId());
                    ps.setLong(3, i.amount().toMinor());
                    ps.setString(4, i.amount().currency().getCurrencyCode());
                    ps.setString(5, i.description());
                    ps.setString(6, i.date().toString());
                    ps.setLong(7, i.id());
                });
    }

    @Override
    public Optional<Income> findById(long id) {
        return queryOne("SELECT * FROM income WHERE id=?",
                ps -> ps.setLong(1, id), Mappers::income);
    }

    @Override
    public List<Income> findAll() {
        return query("SELECT * FROM income ORDER BY txn_date DESC, id DESC", NO_ARGS, Mappers::income);
    }

    @Override
    public List<Income> findByDateRange(LocalDate from, LocalDate to) {
        return query("SELECT * FROM income WHERE txn_date BETWEEN ? AND ? ORDER BY txn_date DESC, id DESC",
                ps -> {
                    ps.setString(1, from.toString());
                    ps.setString(2, to.toString());
                }, Mappers::income);
    }

    @Override
    public List<Income> findByMonth(YearMonth month) {
        return findByDateRange(month.atDay(1), month.atEndOfMonth());
    }

    @Override
    public void deleteById(long id) {
        execute("DELETE FROM income WHERE id=?", ps -> ps.setLong(1, id));
    }
}
