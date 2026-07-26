package com.expense.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expense.android.repository.FinanceRepository
import com.expense.core.domain.Account
import com.expense.core.domain.AccountType
import com.expense.core.domain.Category
import com.expense.core.domain.CategoryType
import com.expense.core.domain.PaymentMethod
import com.expense.core.domain.PaymentMethodType
import com.expense.core.exception.ExpenseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

/** UI state for the Manage screen: the full (including archived) reference lists. */
data class ManageUiState(
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val status: String = "",
    val loading: Boolean = false,
)

/**
 * ViewModel for the Manage screen — the Android counterpart of the desktop
 * Manage view. Creates, renames and archives accounts, categories and payment
 * methods through the shared core services, which own the uniqueness and
 * validation rules; nothing is re-implemented here.
 */
class ManageViewModel(
    private val repository: FinanceRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ManageUiState())
    val state: StateFlow<ManageUiState> = _state.asStateFlow()

    fun load() {
        _state.value = _state.value.copy(loading = true)
        viewModelScope.launch {
            val loaded = withContext(Dispatchers.IO) {
                Triple(
                    repository.allAccounts(),
                    repository.allCategories(),
                    repository.paymentMethods(),
                )
            }
            _state.value = _state.value.copy(
                accounts = loaded.first,
                categories = loaded.second,
                paymentMethods = loaded.third,
                loading = false,
            )
        }
    }

    fun createAccount(name: String, type: AccountType, openingBalanceText: String) {
        val opening = if (openingBalanceText.isBlank()) {
            BigDecimal.ZERO
        } else {
            try {
                BigDecimal(openingBalanceText.trim())
            } catch (e: NumberFormatException) {
                _state.value = _state.value.copy(status = "Opening balance must be a number")
                return
            }
        }
        mutate("Account added") { repository.createAccount(name, type, opening) }
    }

    fun setAccountArchived(id: Long, archived: Boolean) =
        mutate(if (archived) "Account archived" else "Account restored") {
            repository.setAccountArchived(id, archived)
        }

    fun createCategory(name: String, type: CategoryType) =
        mutate("Category added") { repository.createCategory(name, type) }

    fun renameCategory(id: Long, newName: String) =
        mutate("Category renamed") { repository.renameCategory(id, newName) }

    fun setCategoryArchived(id: Long, archived: Boolean) =
        mutate(if (archived) "Category archived" else "Category restored") {
            repository.setCategoryArchived(id, archived)
        }

    fun createPaymentMethod(name: String, type: PaymentMethodType) =
        mutate("Payment method added") { repository.createPaymentMethod(name, type) }

    fun setPaymentMethodArchived(id: Long, archived: Boolean) =
        mutate(if (archived) "Payment method archived" else "Payment method restored") {
            repository.setPaymentMethodArchived(id, archived)
        }

    /**
     * Runs a core mutation off the main thread, reports [successMessage] or the
     * core's own validation message, and reloads the lists.
     */
    private fun mutate(successMessage: String, action: () -> Unit) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { action() }
                _state.value = _state.value.copy(status = successMessage)
                load()
            } catch (e: ExpenseException) {
                _state.value = _state.value.copy(status = e.message ?: "Could not save")
            } catch (e: IllegalArgumentException) {
                _state.value = _state.value.copy(status = e.message ?: "Could not save")
            }
        }
    }
}
