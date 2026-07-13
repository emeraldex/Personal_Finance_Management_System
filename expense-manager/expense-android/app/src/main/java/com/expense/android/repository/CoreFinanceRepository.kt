package com.expense.android.repository

import com.expense.core.domain.Account
import com.expense.core.domain.Category
import com.expense.core.domain.CategoryType
import com.expense.core.domain.Expense
import com.expense.core.domain.Income
import com.expense.core.domain.PaymentMethod
import com.expense.core.domain.Transaction
import com.expense.core.dto.CreateBudgetRequest
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
    private val currency: Currency = Currency.getInstance("MYR"),
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

    override fun accounts(): List<Account> =
        manager.accounts().list().filter { !it.archived() }

    override fun expenseCategories(): List<Category> =
        manager.categories().listByType(CategoryType.EXPENSE).filter { !it.archived() }

    override fun incomeCategories(): List<Category> =
        manager.categories().listByType(CategoryType.INCOME).filter { !it.archived() }

    override fun paymentMethods(): List<PaymentMethod> =
        manager.paymentMethods().list()

    override fun transactions(month: YearMonth): List<Transaction> {
        val expenses: List<Transaction> = manager.expenses().listByMonth(month)
        val incomes: List<Transaction> = manager.incomes().listByMonth(month)
        return (expenses + incomes).sortedByDescending { it.date() }
    }

    override fun deleteTransaction(transaction: Transaction) {
        when (transaction) {
            is Expense -> manager.expenses().delete(transaction.id())
            is Income -> manager.incomes().delete(transaction.id())
            else -> throw IllegalArgumentException("Unknown transaction type")
        }
    }

    override fun setBudget(categoryId: Long, month: YearMonth, limit: BigDecimal) {
        manager.budgets().set(CreateBudgetRequest(categoryId, month, Money.of(limit, currency)))
    }
}
