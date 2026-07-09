package com.expense.core.repository;

import com.expense.core.database.ConnectionProvider;
import com.expense.core.domain.MonthlySummarySnapshot;
import com.expense.core.mapper.Mappers;

import java.time.YearMonth;
import java.util.Optional;

/** SQLite/JDBC implementation of {@link MonthlySummaryRepository}. */
public final class JdbcMonthlySummaryRepository extends JdbcSupport implements MonthlySummaryRepository {

    public JdbcMonthlySummaryRepository(ConnectionProvider connections) {
        super(connections);
    }

    @Override
    public MonthlySummarySnapshot upsert(MonthlySummarySnapshot s) {
        long id = insertReturning(
                "INSERT INTO monthly_summary(month, total_income_minor, total_expense_minor, net_minor, "
                        + "savings_minor, outstanding_minor, currency, generated_at) VALUES (?,?,?,?,?,?,?,?) "
                        + "ON CONFLICT(month) DO UPDATE SET "
                        + "total_income_minor=excluded.total_income_minor, "
                        + "total_expense_minor=excluded.total_expense_minor, "
                        + "net_minor=excluded.net_minor, savings_minor=excluded.savings_minor, "
                        + "outstanding_minor=excluded.outstanding_minor, currency=excluded.currency, "
                        + "generated_at=excluded.generated_at RETURNING id",
                ps -> {
                    ps.setString(1, s.month().toString());
                    ps.setLong(2, s.totalIncome().toMinor());
                    ps.setLong(3, s.totalExpense().toMinor());
                    ps.setLong(4, s.netBalance().toMinor());
                    ps.setLong(5, s.savings().toMinor());
                    ps.setLong(6, s.outstanding().toMinor());
                    ps.setString(7, s.totalIncome().currency().getCurrencyCode());
                    ps.setString(8, s.generatedAt().toString());
                });
        return s.withId(id);
    }

    @Override
    public Optional<MonthlySummarySnapshot> findByMonth(YearMonth month) {
        return queryOne("SELECT * FROM monthly_summary WHERE month=?",
                ps -> ps.setString(1, month.toString()), Mappers::summary);
    }
}
