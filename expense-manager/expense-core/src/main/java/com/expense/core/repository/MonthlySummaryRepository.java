package com.expense.core.repository;

import com.expense.core.domain.MonthlySummarySnapshot;

import java.time.YearMonth;
import java.util.Optional;

/** Persistence port for cached {@link MonthlySummarySnapshot} rows. */
public interface MonthlySummaryRepository {
    /** Inserts or replaces the snapshot for its month. */
    MonthlySummarySnapshot upsert(MonthlySummarySnapshot snapshot);

    Optional<MonthlySummarySnapshot> findByMonth(YearMonth month);
}
