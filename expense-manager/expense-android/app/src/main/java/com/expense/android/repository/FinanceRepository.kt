package com.expense.android.repository

import com.expense.core.report.MonthlySummary
import java.time.YearMonth

/**
 * Android-facing data port used by ViewModels. It is intentionally narrow and
 * UI-shaped; concrete implementations delegate to the shared `expense-core`
 * services. Keeping the ViewModel dependent on this interface (not on a concrete
 * database) means screens can be previewed and unit-tested with a fake.
 */
interface FinanceRepository {
    /** Computes the rich monthly summary via the shared core analytics engine. */
    fun monthlySummary(month: YearMonth): MonthlySummary

    /** Records a quick expense; [amount] is a positive magnitude, stored negative by the core. */
    fun addExpense(accountId: Long, categoryId: Long?, amount: java.math.BigDecimal, description: String)

    /** Records a quick income; [amount] is a positive magnitude. */
    fun addIncome(accountId: Long, categoryId: Long?, amount: java.math.BigDecimal, description: String)
}
