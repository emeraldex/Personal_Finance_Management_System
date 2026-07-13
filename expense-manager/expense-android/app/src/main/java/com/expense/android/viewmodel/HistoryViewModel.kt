package com.expense.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expense.android.repository.FinanceRepository
import com.expense.core.domain.Transaction
import com.expense.core.domain.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.YearMonth

/** A display-ready transaction row carrying its source [Transaction] for deletion. */
data class HistoryRow(
    val transaction: Transaction,
    val date: String,
    val type: String,
    val amount: String,
    val category: String,
    val account: String,
)

/** UI state for the history screen. */
data class HistoryUiState(
    val month: YearMonth = YearMonth.now(),
    val rows: List<HistoryRow> = emptyList(),
    val loading: Boolean = false,
)

/** ViewModel for the transaction-history screen: lists a month and deletes rows. */
class HistoryViewModel(
    private val repository: FinanceRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()

    fun load(month: YearMonth = _state.value.month) {
        _state.value = _state.value.copy(loading = true, month = month)
        viewModelScope.launch {
            val rows = withContext(Dispatchers.IO) { buildRows(month) }
            _state.value = HistoryUiState(month = month, rows = rows, loading = false)
        }
    }

    fun nextMonth() = load(_state.value.month.plusMonths(1))

    fun prevMonth() = load(_state.value.month.minusMonths(1))

    fun delete(transaction: Transaction) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repository.deleteTransaction(transaction) }
            load(_state.value.month)
        }
    }

    private fun buildRows(month: YearMonth): List<HistoryRow> {
        val accountNames = repository.accounts().associate { it.id() to it.name() }
        val categoryNames = (repository.expenseCategories() + repository.incomeCategories())
            .associate { it.id() to it.name() }
        return repository.transactions(month).map { tx ->
            HistoryRow(
                transaction = tx,
                date = tx.date().toString(),
                type = if (tx.type() == TransactionType.EXPENSE) "Expense" else "Income",
                amount = tx.signedAmount().toString(),
                category = tx.categoryId()?.let { categoryNames[it] } ?: "Uncategorised",
                account = accountNames[tx.accountId()] ?: "?",
            )
        }
    }
}
