package com.expense.android.repository

import com.expense.core.domain.Account
import com.expense.core.domain.AccountType
import com.expense.core.domain.Category
import com.expense.core.domain.CategoryType
import com.expense.core.domain.PaymentMethod
import com.expense.core.domain.PaymentMethodType
import com.expense.core.domain.Transaction
import com.expense.core.network.CategorySuggestion
import com.expense.core.report.MonthlySummary
import java.math.BigDecimal
import java.time.LocalDate
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

    /** Records an expense; [amount] is a positive magnitude, stored negative by the core. */
    fun addExpense(
        accountId: Long,
        categoryId: Long?,
        paymentMethodId: Long?,
        amount: BigDecimal,
        description: String,
        date: LocalDate,
    )

    /** Records an income; [amount] is a positive magnitude. */
    fun addIncome(
        accountId: Long,
        categoryId: Long?,
        amount: BigDecimal,
        description: String,
        date: LocalDate,
    )

    /** Replaces an existing expense in full; [amount] is a positive magnitude. */
    fun updateExpense(
        id: Long,
        accountId: Long,
        categoryId: Long?,
        paymentMethodId: Long?,
        amount: BigDecimal,
        description: String,
        date: LocalDate,
    )

    /** Replaces an existing income in full; [amount] is a positive magnitude. */
    fun updateIncome(
        id: Long,
        accountId: Long,
        categoryId: Long?,
        amount: BigDecimal,
        description: String,
        date: LocalDate,
    )

    /** Active accounts, for entry pickers (archived excluded). */
    fun accounts(): List<Account>

    /** Expense-typed categories, for entry pickers (archived excluded). */
    fun expenseCategories(): List<Category>

    /** Income-typed categories, for entry pickers (archived excluded). */
    fun incomeCategories(): List<Category>

    /** Active payment methods, for the expense entry picker (archived excluded). */
    fun activePaymentMethods(): List<PaymentMethod>

    /** Every payment method including archived ones — name resolution and Manage. */
    fun paymentMethods(): List<PaymentMethod>

    /** Every account including archived ones, for the Manage screen. */
    fun allAccounts(): List<Account>

    /** Every category including archived ones, for the Manage screen. */
    fun allCategories(): List<Category>

    /** Creates an account with the given opening balance. */
    fun createAccount(name: String, type: AccountType, openingBalance: BigDecimal): Account

    /** Archives or restores an account; archived accounts disappear from pickers. */
    fun setAccountArchived(id: Long, archived: Boolean)

    /** Creates a category of the given type. */
    fun createCategory(name: String, type: CategoryType): Category

    /** Renames a category, keeping every transaction that references it. */
    fun renameCategory(id: Long, newName: String)

    /** Archives or restores a category. */
    fun setCategoryArchived(id: Long, archived: Boolean)

    /** Creates a payment method. */
    fun createPaymentMethod(name: String, type: PaymentMethodType): PaymentMethod

    /** Archives or restores a payment method. */
    fun setPaymentMethodArchived(id: Long, archived: Boolean)

    /** The month's expenses and income as a single list, newest first. */
    fun transactions(month: YearMonth): List<Transaction>

    /** Deletes a transaction, dispatching to the correct core service by type. */
    fun deleteTransaction(transaction: Transaction)

    /** Sets (creates or replaces) the monthly cap for an expense category. */
    fun setBudget(categoryId: Long, month: YearMonth, limit: BigDecimal)

    /**
     * Suggests a category for a free-text description using the core's offline
     * categoriser seam. Returns `null` when no candidate clears the confidence bar.
     */
    fun suggestCategory(description: String, candidates: List<Category>): CategorySuggestion?

    /** The month's transactions rendered as CSV by the shared core exporter. */
    fun transactionsCsv(month: YearMonth): String

    /** The month's summary (totals and breakdowns) rendered as CSV by the core exporter. */
    fun summaryCsv(month: YearMonth): String
}
