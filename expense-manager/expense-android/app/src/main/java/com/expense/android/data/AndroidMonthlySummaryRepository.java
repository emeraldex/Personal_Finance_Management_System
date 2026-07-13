package com.expense.android.data;

import com.expense.core.domain.MonthlySummarySnapshot;
import com.expense.core.repository.MonthlySummaryRepository;

import java.time.YearMonth;
import java.util.Optional;

/**
 * {@code android.database.sqlite} implementation of {@link MonthlySummaryRepository},
 * with the same explicit read-then-write upsert as {@link AndroidBudgetRepository}
 * to stay compatible with older Android SQLite versions.
 */
public final class AndroidMonthlySummaryRepository extends AndroidSqlSupport
        implements MonthlySummaryRepository {

    public AndroidMonthlySummaryRepository(AndroidDatabase database) {
        super(database);
    }

    @Override
    public MonthlySummarySnapshot upsert(MonthlySummarySnapshot s) {
        Optional<MonthlySummarySnapshot> existing = findByMonth(s.month());
        long id;
        if (existing.isPresent()) {
            id = existing.get().id();
            execute("UPDATE monthly_summary SET total_income_minor=?, total_expense_minor=?, net_minor=?, "
                            + "savings_minor=?, outstanding_minor=?, currency=?, generated_at=? WHERE id=?",
                    s.totalIncome().toMinor(), s.totalExpense().toMinor(), s.netBalance().toMinor(),
                    s.savings().toMinor(), s.outstanding().toMinor(),
                    s.totalIncome().currency().getCurrencyCode(), s.generatedAt().toString(), id);
        } else {
            id = insert("INSERT INTO monthly_summary(month, total_income_minor, total_expense_minor, "
                            + "net_minor, savings_minor, outstanding_minor, currency, generated_at) "
                            + "VALUES (?,?,?,?,?,?,?,?)",
                    s.month().toString(), s.totalIncome().toMinor(), s.totalExpense().toMinor(),
                    s.netBalance().toMinor(), s.savings().toMinor(), s.outstanding().toMinor(),
                    s.totalIncome().currency().getCurrencyCode(), s.generatedAt().toString());
        }
        return s.withId(id);
    }

    @Override
    public Optional<MonthlySummarySnapshot> findByMonth(YearMonth month) {
        return queryOne("SELECT * FROM monthly_summary WHERE month=?",
                args(month.toString()), AndroidMappers::summary);
    }
}
