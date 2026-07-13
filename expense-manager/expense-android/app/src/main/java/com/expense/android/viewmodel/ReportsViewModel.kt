package com.expense.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expense.android.repository.FinanceRepository
import com.expense.core.domain.Category
import com.expense.core.exception.ExpenseException
import com.expense.core.report.MonthlySummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.time.YearMonth

/** UI state for the reports screen. */
data class ReportsUiState(
    val month: YearMonth = YearMonth.now(),
    val summary: MonthlySummary? = null,
    val expenseCategories: List<Category> = emptyList(),
    val status: String = "",
    val loading: Boolean = false,
)

/**
 * ViewModel for the reports screen. Surfaces the rich [MonthlySummary]
 * breakdowns (category, payment method, budget utilisation, cash flow) and lets
 * the user set a per-category monthly cap via the shared core.
 */
class ReportsViewModel(
    private val repository: FinanceRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ReportsUiState())
    val state: StateFlow<ReportsUiState> = _state.asStateFlow()

    fun load(month: YearMonth = _state.value.month) {
        _state.value = _state.value.copy(loading = true, month = month)
        viewModelScope.launch {
            val summary = withContext(Dispatchers.IO) { repository.monthlySummary(month) }
            val categories = withContext(Dispatchers.IO) { repository.expenseCategories() }
            _state.value = _state.value.copy(
                month = month, summary = summary, expenseCategories = categories, loading = false,
            )
        }
    }

    fun nextMonth() = load(_state.value.month.plusMonths(1))

    fun prevMonth() = load(_state.value.month.minusMonths(1))

    fun setBudget(categoryId: Long, limitText: String) {
        val limit = try {
            BigDecimal(limitText.trim())
        } catch (e: NumberFormatException) {
            _state.value = _state.value.copy(status = "Limit must be a number")
            return
        }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { repository.setBudget(categoryId, _state.value.month, limit) }
                _state.value = _state.value.copy(status = "Budget saved")
                load(_state.value.month)
            } catch (e: ExpenseException) {
                _state.value = _state.value.copy(status = e.message ?: "Could not save budget")
            }
        }
    }
}
