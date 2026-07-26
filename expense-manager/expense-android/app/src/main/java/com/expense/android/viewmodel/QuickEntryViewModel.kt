package com.expense.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expense.android.repository.FinanceRepository
import com.expense.core.domain.Account
import com.expense.core.domain.Category
import com.expense.core.domain.PaymentMethod
import com.expense.core.exception.ExpenseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Selectable options for the entry forms. */
data class EntryOptions(
    val accounts: List<Account> = emptyList(),
    val expenseCategories: List<Category> = emptyList(),
    val incomeCategories: List<Category> = emptyList(),
    val paymentMethods: List<PaymentMethod> = emptyList(),
)

/**
 * A resolved category suggestion from the core's offline categoriser: the
 * category to preselect plus the name and confidence shown to the user.
 */
data class CategoryHint(
    val categoryId: Long,
    val categoryName: String,
    val confidence: Double,
)

/** ViewModel powering the Quick Expense / Quick Income screens. */
class QuickEntryViewModel(
    private val repository: FinanceRepository,
) : ViewModel() {

    private val _status = MutableStateFlow("")
    val status: StateFlow<String> = _status.asStateFlow()

    private val _options = MutableStateFlow(EntryOptions())
    val options: StateFlow<EntryOptions> = _options.asStateFlow()

    private val _hint = MutableStateFlow<CategoryHint?>(null)
    val hint: StateFlow<CategoryHint?> = _hint.asStateFlow()

    /** Loads accounts, categories and payment methods for the pickers. */
    fun loadOptions() {
        viewModelScope.launch {
            val opts = withContext(Dispatchers.IO) {
                EntryOptions(
                    accounts = repository.accounts(),
                    expenseCategories = repository.expenseCategories(),
                    incomeCategories = repository.incomeCategories(),
                    paymentMethods = repository.activePaymentMethods(),
                )
            }
            _options.value = opts
        }
    }

    /**
     * Asks the core categoriser which expense category best matches [description]
     * and publishes it as a [hint]. Blank input clears any previous suggestion.
     */
    fun suggestCategory(description: String) {
        if (description.isBlank()) {
            _hint.value = null
            return
        }
        viewModelScope.launch {
            val candidates = _options.value.expenseCategories
            val suggestion = withContext(Dispatchers.IO) {
                repository.suggestCategory(description, candidates)
            }
            _hint.value = suggestion?.let { s ->
                candidates.firstOrNull { it.id() == s.categoryId() }
                    ?.let { CategoryHint(s.categoryId(), it.name(), s.confidence()) }
            }
        }
    }

    fun addExpense(
        accountId: Long,
        categoryId: Long?,
        paymentMethodId: Long?,
        amountText: String,
        description: String,
        dateText: String,
    ) = submit(amountText, dateText) { amount, date ->
        repository.addExpense(accountId, categoryId, paymentMethodId, amount, description, date)
    }

    fun addIncome(
        accountId: Long,
        categoryId: Long?,
        amountText: String,
        description: String,
        dateText: String,
    ) = submit(amountText, dateText) { amount, date ->
        repository.addIncome(accountId, categoryId, amount, description, date)
    }

    private fun submit(
        amountText: String,
        dateText: String,
        action: (BigDecimal, LocalDate) -> Unit,
    ) {
        val amount = amountText.toBigDecimalOrNull()
        if (amount == null) {
            _status.value = "Amount must be a number"
            return
        }
        val date = dateText.toLocalDateOrNull()
        if (date == null) {
            _status.value = "Date must look like 2026-07-26"
            return
        }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { action(amount, date) }
                _status.value = "Saved"
                _hint.value = null
            } catch (e: ExpenseException) {
                _status.value = e.message ?: "Could not save"
            }
        }
    }

    private fun String.toBigDecimalOrNull(): BigDecimal? =
        try { BigDecimal(this.trim()) } catch (e: NumberFormatException) { null }

    private fun String.toLocalDateOrNull(): LocalDate? =
        try { LocalDate.parse(this.trim()) } catch (e: DateTimeParseException) { null }
}
