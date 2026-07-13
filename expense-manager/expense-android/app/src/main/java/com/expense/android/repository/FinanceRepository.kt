package com.expense.android.repository

import com.expense.core.domain.Account
import com.expense.core.domain.Category
import com.expense.core.domain.PaymentMethod
import com.expense.core.domain.Transaction
import com.expense.core.report.MonthlySummary
import java.math.BigDecimal
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
    fun addExpense(accountId: Long, categoryId: Long?, amount: BigDecimal, description: String)

    /** Records a quick income; [amount] is a positive magnitude. */
    fun addIncome(accountId: Long, categoryId: Long?, amount: BigDecimal, description: String)

    /** All accounts, for entry pickers (archived excluded). */
    fun accounts(): List<Account>

    /** Expense-typed categories, for entry pickers (archived excluded). */
    fun expenseCategories(): List<Category>

    /** Income-typed categories, for entry pickers (archived excluded). */
    fun incomeCategories(): List<Category>

    /** All payment methods, used to resolve names in history. */
    fun paymentMethods(): List<PaymentMethod>

    /** The month's expenses and income as a single list, newest first. */
    fun transactions(month: YearMonth): List<Transaction>

    /** Deletes a transaction, dispatching to the correct core service by type. */
    fun deleteTransaction(transaction: Transaction)

    /** Sets (creates or replaces) the monthly cap for an expense category. */
    fun setBudget(categoryId: Long, month: YearMonth, limit: BigDecimal)
}
