package com.expense.android.repository

import com.expense.core.domain.Account
import com.expense.core.domain.AccountType
import com.expense.core.domain.Category
import com.expense.core.domain.CategoryType
import com.expense.core.domain.Expense
import com.expense.core.domain.Income
import com.expense.core.domain.PaymentMethod
import com.expense.core.domain.PaymentMethodType
import com.expense.core.domain.Transaction
import com.expense.core.dto.CreateAccountRequest
import com.expense.core.dto.CreateBudgetRequest
import com.expense.core.dto.CreateCategoryRequest
import com.expense.core.dto.CreateExpenseRequest
import com.expense.core.dto.CreateIncomeRequest
import com.expense.core.dto.CreatePaymentMethodRequest
import com.expense.core.dto.UpdateExpenseRequest
import com.expense.core.dto.UpdateIncomeRequest
import com.expense.core.network.CategorySuggestion
import com.expense.core.report.MonthlySummary
import com.expense.core.report.MonthlySummaryCsvExporter
import com.expense.core.report.TransactionCsvExporter
import com.expense.core.service.ExpenseManager
import com.expense.core.util.Money
import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.util.Currency

/**
 * Production [FinanceRepository] that delegates every operation to the shared
 * core [ExpenseManager]. This is the single seam where the Android app meets the
 * business logic: no rules are duplicated here.
 *
 * The [ExpenseManager] is created by `data/CoreProvider`, which wires the
 * `android.database.sqlite` adapters into the same services the desktop uses.
 * Categorisation and CSV rendering are likewise core concerns — this class only
 * adapts their shapes to what the ViewModels want.
 */
class CoreFinanceRepository(
    private val manager: ExpenseManager,
    private val currency: Currency = Currency.getInstance("MYR"),
) : FinanceRepository {

    override fun monthlySummary(month: YearMonth): MonthlySummary =
        manager.summaries().summarize(month)

    override fun addExpense(
        accountId: Long,
        categoryId: Long?,
        paymentMethodId: Long?,
        amount: BigDecimal,
        description: String,
        date: LocalDate,
    ) {
        manager.expenses().create(
            CreateExpenseRequest(
                accountId, categoryId, paymentMethodId,
                Money.of(amount, currency), description, date,
            )
        )
    }

    override fun addIncome(
        accountId: Long,
        categoryId: Long?,
        amount: BigDecimal,
        description: String,
        date: LocalDate,
    ) {
        manager.incomes().create(
            CreateIncomeRequest(accountId, categoryId, Money.of(amount, currency), description, date)
        )
    }

    override fun updateExpense(
        id: Long,
        accountId: Long,
        categoryId: Long?,
        paymentMethodId: Long?,
        amount: BigDecimal,
        description: String,
        date: LocalDate,
    ) {
        manager.expenses().update(
            UpdateExpenseRequest(
                id, accountId, categoryId, paymentMethodId,
                Money.of(amount, currency), description, date,
            )
        )
    }

    override fun updateIncome(
        id: Long,
        accountId: Long,
        categoryId: Long?,
        amount: BigDecimal,
        description: String,
        date: LocalDate,
    ) {
        manager.incomes().update(
            UpdateIncomeRequest(id, accountId, categoryId, Money.of(amount, currency), description, date)
        )
    }

    override fun accounts(): List<Account> =
        manager.accounts().list().filter { !it.archived() }

    override fun expenseCategories(): List<Category> =
        manager.categories().listByType(CategoryType.EXPENSE).filter { !it.archived() }

    override fun incomeCategories(): List<Category> =
        manager.categories().listByType(CategoryType.INCOME).filter { !it.archived() }

    override fun activePaymentMethods(): List<PaymentMethod> =
        manager.paymentMethods().list().filter { !it.archived() }

    override fun paymentMethods(): List<PaymentMethod> =
        manager.paymentMethods().list()

    override fun allAccounts(): List<Account> =
        manager.accounts().list()

    override fun allCategories(): List<Category> =
        manager.categories().list()

    override fun createAccount(name: String, type: AccountType, openingBalance: BigDecimal): Account =
        manager.accounts().create(
            CreateAccountRequest(name, type, Money.of(openingBalance, currency))
        )

    override fun setAccountArchived(id: Long, archived: Boolean) {
        manager.accounts().setArchived(id, archived)
    }

    override fun createCategory(name: String, type: CategoryType): Category =
        manager.categories().create(CreateCategoryRequest(name, type, null, null))

    override fun renameCategory(id: Long, newName: String) {
        manager.categories().rename(id, newName)
    }

    override fun setCategoryArchived(id: Long, archived: Boolean) {
        manager.categories().setArchived(id, archived)
    }

    override fun createPaymentMethod(name: String, type: PaymentMethodType): PaymentMethod =
        manager.paymentMethods().create(CreatePaymentMethodRequest(name, type))

    override fun setPaymentMethodArchived(id: Long, archived: Boolean) {
        manager.paymentMethods().setArchived(id, archived)
    }

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

    override fun suggestCategory(description: String, candidates: List<Category>): CategorySuggestion? =
        manager.categorizer().suggest(description, candidates).orElse(null)

    override fun transactionsCsv(month: YearMonth): String {
        val out = ByteArrayOutputStream()
        TransactionCsvExporter().export(transactions(month), out)
        return String(out.toByteArray(), Charsets.UTF_8)
    }

    override fun summaryCsv(month: YearMonth): String {
        val out = ByteArrayOutputStream()
        MonthlySummaryCsvExporter().export(monthlySummary(month), out)
        return String(out.toByteArray(), Charsets.UTF_8)
    }
}
