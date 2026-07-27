package com.expense.android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expense.android.data.AppPrefs
import com.expense.android.data.CoreProvider
import com.expense.android.navigation.AppNavigation
import com.expense.android.notify.AndroidNotificationPublisher
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
import com.expense.core.network.CsvStatementBankFeedClient
import com.expense.core.network.NotificationPublisher
import com.expense.core.service.BudgetAlertService
import com.expense.core.service.ExpenseManager
import com.expense.core.util.Money
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
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

        // NotificationPublisher seam: alerts post to a notification channel; the
        // Settings toggle is consulted at publish time so turning alerts off
        // applies immediately.
        val prefs = AppPrefs(applicationContext)

        // Seed the core's fixed-rate table from persisted preferences
        // (1 unit of <code> = <rate> units of the app currency).
        prefs.fxRates().forEach { (code, rate) ->
            try {
                manager.exchangeRates().setRate(Currency.getInstance(code), currency, rate)
            } catch (ignored: IllegalArgumentException) {
                // A bad hand-edited code or rate is dropped rather than breaking startup.
            }
        }

        val channel = AndroidNotificationPublisher(applicationContext)
        val publisher = NotificationPublisher { n -> if (prefs.budgetAlerts) channel.publish(n) }
        val budgetAlerts = BudgetAlertService(manager.summaries(), publisher)
        requestNotificationPermissionIfNeeded()

        val repository: FinanceRepository = CoreFinanceRepository(manager, currency, budgetAlerts)
        val factory = FinanceViewModelFactory(repository)
        val storagePath = File(applicationContext.filesDir, "expenses.db").absolutePath

        // Bank-feed seam: the offline default parses downloaded CSV statements;
        // an open-banking client can replace it behind the same interface.
        val bankFeed = CsvStatementBankFeedClient(currency)
        val settingsAccounts = manager.accounts().list().filter { !it.archived() }

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
                        budgetAlertsEnabled = prefs.budgetAlerts,
                        onBudgetAlertsChange = { prefs.budgetAlerts = it },
                        fxRates = prefs.fxRates(),
                        onSetFxRate = { code, rate ->
                            prefs.putFxRate(code, rate)
                            manager.exchangeRates().setRate(Currency.getInstance(code), currency, rate)
                        },
                        onRemoveFxRate = { code ->
                            prefs.removeFxRate(code)
                            manager.exchangeRates().removeRate(Currency.getInstance(code), currency)
                        },
                        accounts = settingsAccounts,
                        onImportBankStatement = { accountId, uri ->
                            importBankStatement(manager, bankFeed, budgetAlerts, accountId, uri)
                        },
                    )
                }
            }
        }
    }

    /**
     * Copies the picked document to a cache file (the core parser reads file
     * paths, not content URIs) and imports it through the bank-feed seam,
     * re-checking affected budgets. Returns a status line for the screen.
     */
    private suspend fun importBankStatement(
        manager: ExpenseManager,
        bankFeed: CsvStatementBankFeedClient,
        budgetAlerts: BudgetAlertService,
        accountId: Long,
        uri: android.net.Uri,
    ): String = withContext(Dispatchers.IO) {
        try {
            val cache = File(cacheDir, "bank-statement-import.csv")
            val input = contentResolver.openInputStream(uri)
                ?: return@withContext "Could not open the selected file"
            input.use { source -> cache.outputStream().use { source.copyTo(it) } }
            val entries = bankFeed.fetch(cache.absolutePath, LocalDate.of(1970, 1, 1), LocalDate.now())
            val result = manager.bankFeedImports().importInto(accountId, entries, budgetAlerts)
            buildString {
                append("Bank feed: imported ").append(result.imported())
                append(", skipped ").append(result.skipped())
                if (result.warnings().isNotEmpty()) {
                    append(" — ").append(result.warnings().joinToString("; "))
                }
            }
        } catch (e: RuntimeException) {
            "Bank import failed: ${e.message}"
        }
    }

    /** On API 33+ notifications need a runtime grant; earlier versions post freely. */
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
                .launch(Manifest.permission.POST_NOTIFICATIONS)
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
