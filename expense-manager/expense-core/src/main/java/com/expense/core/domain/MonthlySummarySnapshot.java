package com.expense.core.domain;

import com.expense.core.util.Money;

import java.time.Instant;
import java.time.YearMonth;
import java.util.Objects;

/**
 * Persistable, denormalised snapshot of a month's headline figures. This is a
 * cache of values that can always be recomputed from the underlying
 * transactions; it exists so dashboards can load quickly and so historical
 * months can be "closed". Stored in the {@code monthly_summary} table.
 *
 * @param id           persistence id, {@code null} before insertion
 * @param month        the month summarised
 * @param totalIncome  sum of income (non-negative)
 * @param totalExpense sum of expenses (non-positive)
 * @param netBalance   totalIncome + totalExpense
 * @param savings      portion of net balance treated as saved (>= 0)
 * @param outstanding  unpaid/owed amount carried at month end (>= 0)
 * @param generatedAt  when this snapshot was computed
 */
public record MonthlySummarySnapshot(Long id, YearMonth month, Money totalIncome,
                                     Money totalExpense, Money netBalance, Money savings,
                                     Money outstanding, Instant generatedAt) {

    public MonthlySummarySnapshot {
        Objects.requireNonNull(month, "month");
        Objects.requireNonNull(totalIncome, "totalIncome");
        Objects.requireNonNull(totalExpense, "totalExpense");
        Objects.requireNonNull(netBalance, "netBalance");
        Objects.requireNonNull(savings, "savings");
        Objects.requireNonNull(outstanding, "outstanding");
        Objects.requireNonNull(generatedAt, "generatedAt");
    }

    public MonthlySummarySnapshot withId(long newId) {
        return new MonthlySummarySnapshot(newId, month, totalIncome, totalExpense,
                netBalance, savings, outstanding, generatedAt);
    }
}
