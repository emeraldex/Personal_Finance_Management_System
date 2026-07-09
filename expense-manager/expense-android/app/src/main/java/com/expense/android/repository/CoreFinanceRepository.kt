package com.expense.android.repository

import com.expense.core.dto.CreateExpenseRequest
import com.expense.core.dto.CreateIncomeRequest
import com.expense.core.report.MonthlySummary
import com.expense.core.service.ExpenseManager
import com.expense.core.util.Money
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.util.Currency

/**
 * Production [FinanceRepository] that delegates every operation to the shared
 * core [ExpenseManager]. This is the single seam where the Android app meets the
 * business logic: no rules are duplicated here.
 *
 * The [ExpenseManager] is created by `data/CoreProvider`, which opens the core's
 * JDBC/SQLite database against a file in the app's private storage. The same
 * services and schema therefore power both desktop and mobile.
 */
class CoreFinanceRepository(
    private val manager: ExpenseManager,
    private val currency: Currency = Currency.getInstance("USD"),
) : FinanceRepository {

    override fun monthlySummary(month: YearMonth): MonthlySummary =
        manager.summaries().summarize(month)

    override fun addExpense(accountId: Long, categoryId: Long?, amount: BigDecimal, description: String) {
        manager.expenses().create(
            CreateExpenseRequest(accountId, categoryId, null, Money.of(amount, currency), description, LocalDate.now())
        )
    }

    override fun addIncome(accountId: Long, categoryId: Long?, amount: BigDecimal, description: String) {
        manager.incomes().create(
            CreateIncomeRequest(accountId, categoryId, Money.of(amount, currency), description, LocalDate.now())
        )
    }
}
