package com.expense.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expense.android.repository.FinanceRepository
import com.expense.core.domain.Account
import com.expense.core.domain.Category
import com.expense.core.domain.Expense
import com.expense.core.domain.PaymentMethod
import com.expense.core.domain.Transaction
import com.expense.core.domain.TransactionType
import com.expense.core.exception.ExpenseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeParseException

/** A display-ready transaction row carrying its source [Transaction] for edit and delete. */
data class HistoryRow(
    val transaction: Transaction,
    val date: String,
    val type: String,
    val amount: String,
    val category: String,
    val account: String,
    val paymentMethod: String?,
)

/** UI state for the history screen. */
data class HistoryUiState(
    val month: YearMonth = YearMonth.now(),
    val rows: List<HistoryRow> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val expenseCategories: List<Category> = emptyList(),
    val incomeCategories: List<Category> = emptyList(),
    val paymentMethods: List<PaymentMethod> = emptyList(),
    /** The transaction currently open in the edit dialog, if any. */
    val editing: Transaction? = null,
    val status: String = "",
    val loading: Boolean = false,
)

/**
 * ViewModel for the transaction-history screen: lists a month and edits or
 * deletes rows. Every mutation goes through the shared core services, so the
 * same validation the desktop enforces applies here.
 */
class HistoryViewModel(
    private val repository: FinanceRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()

    fun load(month: YearMonth = _state.value.month) {
        _state.value = _state.value.copy(loading = true, month = month)
        viewModelScope.launch {
            val loaded = withContext(Dispatchers.IO) {
                Loaded(
                    rows = buildRows(month),
                    accounts = repository.accounts(),
                    expenseCategories = repository.expenseCategories(),
                    incomeCategories = repository.incomeCategories(),
                    paymentMethods = repository.activePaymentMethods(),
                )
            }
            _state.value = _state.value.copy(
                month = month,
                rows = loaded.rows,
                accounts = loaded.accounts,
                expenseCategories = loaded.expenseCategories,
                incomeCategories = loaded.incomeCategories,
                paymentMethods = loaded.paymentMethods,
                loading = false,
            )
        }
    }

    fun nextMonth() = load(_state.value.month.plusMonths(1))

    fun prevMonth() = load(_state.value.month.minusMonths(1))

    /** Opens the edit dialog for [transaction]. */
    fun beginEdit(transaction: Transaction) {
        _state.value = _state.value.copy(editing = transaction, status = "")
    }

    /** Closes the edit dialog without saving. */
    fun cancelEdit() {
        _state.value = _state.value.copy(editing = null)
    }

    /**
     * Applies an edit through the core update services. [amountText] is a positive
     * magnitude; the core re-applies the expense/income sign convention.
     */
    fun saveEdit(
        transaction: Transaction,
        accountId: Long,
        categoryId: Long?,
        paymentMethodId: Long?,
        amountText: String,
        description: String,
        dateText: String,
    ) {
        val amount = try {
            BigDecimal(amountText.trim())
        } catch (e: NumberFormatException) {
            _state.value = _state.value.copy(status = "Amount must be a number")
            return
        }
        val date = try {
            LocalDate.parse(dateText.trim())
        } catch (e: DateTimeParseException) {
            _state.value = _state.value.copy(status = "Date must look like 2026-07-26")
            return
        }
        val id = transaction.id()
        if (id == null) {
            _state.value = _state.value.copy(status = "This transaction has no id")
            return
        }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    if (transaction.type() == TransactionType.EXPENSE) {
                        repository.updateExpense(
                            id, accountId, categoryId, paymentMethodId, amount, description, date,
                        )
                    } else {
                        repository.updateIncome(id, accountId, categoryId, amount, description, date)
                    }
                }
                _state.value = _state.value.copy(editing = null, status = "Saved")
                load(_state.value.month)
            } catch (e: ExpenseException) {
                _state.value = _state.value.copy(status = e.message ?: "Could not save")
            }
        }
    }

    fun delete(transaction: Transaction) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repository.deleteTransaction(transaction) }
            load(_state.value.month)
        }
    }

    /** Everything one background pass gathers, so the UI updates in a single hop. */
    private data class Loaded(
        val rows: List<HistoryRow>,
        val accounts: List<Account>,
        val expenseCategories: List<Category>,
        val incomeCategories: List<Category>,
        val paymentMethods: List<PaymentMethod>,
    )

    private fun buildRows(month: YearMonth): List<HistoryRow> {
        // Names resolve against the full lists so archived accounts, categories and
        // payment methods still label their historical transactions.
        val accountNames = repository.allAccounts().associate { it.id() to it.name() }
        val categoryNames = repository.allCategories().associate { it.id() to it.name() }
        val paymentNames = repository.paymentMethods().associate { it.id() to it.name() }
        return repository.transactions(month).map { tx ->
            HistoryRow(
                transaction = tx,
                date = tx.date().toString(),
                type = if (tx.type() == TransactionType.EXPENSE) "Expense" else "Income",
                amount = tx.signedAmount().toString(),
                category = tx.categoryId()?.let { categoryNames[it] } ?: "Uncategorised",
                account = accountNames[tx.accountId()] ?: "?",
                paymentMethod = (tx as? Expense)?.paymentMethodId()?.let { paymentNames[it] },
            )
        }
    }
}
