package com.expense.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expense.android.repository.FinanceRepository
import com.expense.core.exception.ValidationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

/** ViewModel powering the Quick Expense / Quick Income screens. */
class QuickEntryViewModel(
    private val repository: FinanceRepository,
) : ViewModel() {

    private val _status = MutableStateFlow("")
    val status: StateFlow<String> = _status

    fun addExpense(accountId: Long, categoryId: Long?, amountText: String, description: String) =
        submit(amountText) { amount -> repository.addExpense(accountId, categoryId, amount, description) }

    fun addIncome(accountId: Long, categoryId: Long?, amountText: String, description: String) =
        submit(amountText) { amount -> repository.addIncome(accountId, categoryId, amount, description) }

    private fun submit(amountText: String, action: (BigDecimal) -> Unit) {
        val amount = amountText.toBigDecimalOrNull()
        if (amount == null) {
            _status.value = "Amount must be a number"
            return
        }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { action(amount) }
                _status.value = "Saved"
            } catch (e: ValidationException) {
                _status.value = e.errors().toString()
            }
        }
    }

    private fun String.toBigDecimalOrNull(): BigDecimal? =
        try { BigDecimal(this.trim()) } catch (e: NumberFormatException) { null }
}
