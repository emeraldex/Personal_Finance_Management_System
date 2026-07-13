package com.expense.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expense.android.data.CoreProvider
import com.expense.android.navigation.AppNavigation
import com.expense.android.repository.CoreFinanceRepository
import com.expense.android.repository.FinanceRepository
import com.expense.android.viewmodel.DashboardViewModel
import com.expense.android.viewmodel.HistoryViewModel
import com.expense.android.viewmodel.QuickEntryViewModel
import com.expense.android.viewmodel.ReportsViewModel
import com.expense.core.domain.AccountType
import com.expense.core.domain.CategoryType
import com.expense.core.dto.CreateAccountRequest
import com.expense.core.dto.CreateCategoryRequest
import com.expense.core.service.ExpenseManager
import com.expense.core.util.Money
import java.io.File
import java.util.Currency

/**
 * Single-activity Compose host. It builds the object graph once — shared core
 * [ExpenseManager] → [CoreFinanceRepository] → ViewModels → navigation — and does
 * a tiny first-run bootstrap so the screens have an account and categories to use.
 *
 * All financial behaviour lives in `expense-core`; this activity is pure wiring.
 */
class MainActivity : ComponentActivity() {

    private val currency: Currency = Currency.getInstance("MYR")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val manager = CoreProvider.get(applicationContext)
        val defaultAccountId = bootstrap(manager)
        val repository: FinanceRepository = CoreFinanceRepository(manager, currency)
        val factory = FinanceViewModelFactory(repository)
        val storagePath = File(applicationContext.filesDir, "expenses.db").absolutePath

        setContent {
            MaterialTheme {
                Surface {
                    val dashboardVm: DashboardViewModel = viewModel(factory = factory)
                    val quickVm: QuickEntryViewModel = viewModel(factory = factory)
                    val historyVm: HistoryViewModel = viewModel(factory = factory)
                    val reportsVm: ReportsViewModel = viewModel(factory = factory)
                    AppNavigation(
                        dashboardViewModel = dashboardVm,
                        quickEntryViewModel = quickVm,
                        historyViewModel = historyVm,
                        reportsViewModel = reportsVm,
                        defaultAccountId = defaultAccountId,
                        currencyCode = currency.currencyCode,
                        storagePath = storagePath,
                    )
                }
            }
        }
    }

    /** Ensures a default account and a couple of starter categories exist; returns the account id. */
    private fun bootstrap(manager: ExpenseManager): Long {
        val accounts = manager.accounts().list()
        val accountId = if (accounts.isEmpty()) {
            manager.accounts().create(
                CreateAccountRequest("Cash", AccountType.CASH, Money.zero(currency))
            ).id()
        } else {
            accounts.first().id()
        }
        if (manager.categories().list().isEmpty()) {
            manager.categories().create(CreateCategoryRequest("Groceries", CategoryType.EXPENSE, "#4CAF50", "cart"))
            manager.categories().create(CreateCategoryRequest("Transport", CategoryType.EXPENSE, "#FF9800", "car"))
            manager.categories().create(CreateCategoryRequest("Salary", CategoryType.INCOME, "#2196F3", "wallet"))
        }
        return accountId
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            CoreProvider.shutdown()
        }
    }
}

/**
 * Minimal [ViewModelProvider.Factory] that injects the shared [FinanceRepository]
 * into the finance ViewModels. In a larger app this would be provided by Hilt.
 */
class FinanceViewModelFactory(
    private val repository: FinanceRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
                DashboardViewModel(repository) as T
            modelClass.isAssignableFrom(QuickEntryViewModel::class.java) ->
                QuickEntryViewModel(repository) as T
            modelClass.isAssignableFrom(HistoryViewModel::class.java) ->
                HistoryViewModel(repository) as T
            modelClass.isAssignableFrom(ReportsViewModel::class.java) ->
                ReportsViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
