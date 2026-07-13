package com.expense.android.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.expense.android.ui.DashboardScreen
import com.expense.android.ui.HistoryScreen
import com.expense.android.ui.QuickExpenseScreen
import com.expense.android.ui.QuickIncomeScreen
import com.expense.android.ui.ReportsScreen
import com.expense.android.ui.SettingsScreen
import com.expense.android.viewmodel.DashboardViewModel
import com.expense.android.viewmodel.HistoryViewModel
import com.expense.android.viewmodel.QuickEntryViewModel
import com.expense.android.viewmodel.ReportsViewModel

/** A bottom-navigation destination: route, label and an emoji used as the icon. */
private data class Destination(val route: String, val label: String, val icon: String)

/** Route keys for the app's destinations. */
object Routes {
    const val DASHBOARD = "dashboard"
    const val QUICK_EXPENSE = "quick_expense"
    const val QUICK_INCOME = "quick_income"
    const val HISTORY = "history"
    const val REPORTS = "reports"
    const val SETTINGS = "settings"
}

private val DESTINATIONS = listOf(
    Destination(Routes.DASHBOARD, "Home", "🏠"),
    Destination(Routes.QUICK_EXPENSE, "Expense", "➖"),
    Destination(Routes.QUICK_INCOME, "Income", "➕"),
    Destination(Routes.HISTORY, "History", "📜"),
    Destination(Routes.REPORTS, "Reports", "📊"),
    Destination(Routes.SETTINGS, "Settings", "⚙️"),
)

/**
 * Compose navigation graph with a bottom navigation bar wiring every screen:
 * Dashboard, Add Expense, Add Income, History, Reports and Settings. Each screen
 * hosts a View bound to its ViewModel (MVVM); screens never touch persistence.
 */
@Composable
fun AppNavigation(
    dashboardViewModel: DashboardViewModel,
    quickEntryViewModel: QuickEntryViewModel,
    historyViewModel: HistoryViewModel,
    reportsViewModel: ReportsViewModel,
    defaultAccountId: Long,
    currencyCode: String,
    storagePath: String,
) {
    val nav = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentEntry by nav.currentBackStackEntryAsState()
                val currentDestination = currentEntry?.destination
                DESTINATIONS.forEach { dest ->
                    val selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            nav.navigate(dest.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Text(dest.icon) },
                        label = { Text(dest.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.DASHBOARD) { DashboardScreen(dashboardViewModel) }
            composable(Routes.QUICK_EXPENSE) { QuickExpenseScreen(quickEntryViewModel, defaultAccountId) }
            composable(Routes.QUICK_INCOME) { QuickIncomeScreen(quickEntryViewModel, defaultAccountId) }
            composable(Routes.HISTORY) { HistoryScreen(historyViewModel) }
            composable(Routes.REPORTS) { ReportsScreen(reportsViewModel) }
            composable(Routes.SETTINGS) { SettingsScreen(currencyCode, storagePath) }
        }
    }
}
