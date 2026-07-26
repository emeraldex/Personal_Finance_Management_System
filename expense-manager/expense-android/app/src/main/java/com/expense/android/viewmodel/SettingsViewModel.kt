package com.expense.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expense.android.repository.FinanceRepository
import com.expense.core.exception.ExpenseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.YearMonth

/** A rendered CSV file waiting for the screen to hand it to the Android share sheet. */
data class ExportPayload(val fileName: String, val content: String)

/** UI state for the settings screen. */
data class SettingsUiState(
    val month: YearMonth = YearMonth.now(),
    val status: String = "",
    val exporting: Boolean = false,
    /** Set when an export is ready; the screen shares it and then clears it. */
    val pendingExport: ExportPayload? = null,
)

/**
 * ViewModel for the settings screen. Renders the month's transactions or summary
 * to CSV using the shared core exporters — the same ones the desktop uses — and
 * publishes the result for the screen to share. The ViewModel deliberately knows
 * nothing about Intents or file providers.
 */
class SettingsViewModel(
    private val repository: FinanceRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    fun nextMonth() {
        _state.value = _state.value.copy(month = _state.value.month.plusMonths(1))
    }

    fun prevMonth() {
        _state.value = _state.value.copy(month = _state.value.month.minusMonths(1))
    }

    /** Renders the month's transactions to CSV. */
    fun exportTransactions() {
        val month = _state.value.month
        export("transactions-$month.csv") { repository.transactionsCsv(month) }
    }

    /** Renders the month's summary (totals and breakdowns) to CSV. */
    fun exportSummary() {
        val month = _state.value.month
        export("summary-$month.csv") { repository.summaryCsv(month) }
    }

    /** Called by the screen once the share sheet has been handed the payload. */
    fun exportConsumed() {
        _state.value = _state.value.copy(pendingExport = null)
    }

    private fun export(fileName: String, render: () -> String) {
        _state.value = _state.value.copy(exporting = true, status = "")
        viewModelScope.launch {
            try {
                val content = withContext(Dispatchers.IO) { render() }
                _state.value = _state.value.copy(
                    exporting = false,
                    status = "Exported $fileName",
                    pendingExport = ExportPayload(fileName, content),
                )
            } catch (e: ExpenseException) {
                _state.value = _state.value.copy(
                    exporting = false,
                    status = e.message ?: "Could not export",
                )
            } catch (e: IOException) {
                _state.value = _state.value.copy(
                    exporting = false,
                    status = e.message ?: "Could not export",
                )
            }
        }
    }
}
