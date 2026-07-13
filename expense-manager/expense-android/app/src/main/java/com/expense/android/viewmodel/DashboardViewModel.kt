package com.expense.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expense.android.repository.FinanceRepository
import com.expense.core.report.MonthlySummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.YearMonth

/** UI state for the dashboard. */
data class DashboardUiState(
    val month: YearMonth = YearMonth.now(),
    val summary: MonthlySummary? = null,
    val loading: Boolean = false,
)

/**
 * MVVM ViewModel for the dashboard. Exposes an immutable [StateFlow] the Compose
 * screen collects. All figures come from the shared core via [FinanceRepository];
 * the ViewModel holds no business rules.
 */
class DashboardViewModel(
    private val repository: FinanceRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    fun load(month: YearMonth = _state.value.month) {
        _state.value = _state.value.copy(loading = true, month = month)
        viewModelScope.launch {
            val summary = withContext(Dispatchers.IO) { repository.monthlySummary(month) }
            _state.value = DashboardUiState(month = month, summary = summary, loading = false)
        }
    }

    fun nextMonth() = load(_state.value.month.plusMonths(1))

    fun prevMonth() = load(_state.value.month.minusMonths(1))
}
