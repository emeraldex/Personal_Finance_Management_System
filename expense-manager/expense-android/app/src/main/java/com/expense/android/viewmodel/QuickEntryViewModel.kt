package com.expense.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expense.android.repository.FinanceRepository
import com.expense.core.domain.Account
import com.expense.core.domain.Category
import com.expense.core.exception.ExpenseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

/** Selectable options for the entry forms. */
data class EntryOptions(
    val accounts: List<Account> = emptyList(),
    val expenseCategories: List<Category> = emptyList(),
    val incomeCategories: List<Category> = emptyList(),
)

/** ViewModel powering the Quick Expense / Quick Income screens. */
class QuickEntryViewModel(
    private val repository: FinanceRepository,
) : ViewModel() {

    private val _status = MutableStateFlow("")
    val status: StateFlow<String> = _status.asStateFlow()

    private val _options = MutableStateFlow(EntryOptions())
    val options: StateFlow<EntryOptions> = _options.asStateFlow()

    /** Loads accounts and categories for the pickers. */
    fun loadOptions() {
        viewModelScope.launch {
            val opts = withContext(Dispatchers.IO) {
                EntryOptions(
                    accounts = repository.accounts(),
                    expenseCategories = repository.expenseCategories(),
                    incomeCategories = repository.incomeCategories(),
                )
            }
            _options.value = opts
        }
    }

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
            } catch (e: ExpenseException) {
                _status.value = e.message ?: "Could not save"
            }
        }
    }

    private fun String.toBigDecimalOrNull(): BigDecimal? =
        try { BigDecimal(this.trim()) } catch (e: NumberFormatException) { null }
}
