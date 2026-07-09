package com.expense.android.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.expense.android.ui.DashboardScreen
import com.expense.android.ui.QuickExpenseScreen
import com.expense.android.viewmodel.DashboardViewModel
import com.expense.android.viewmodel.QuickEntryViewModel

/** Route keys for the bottom-level destinations described in the spec. */
object Routes {
    const val DASHBOARD = "dashboard"
    const val QUICK_EXPENSE = "quick_expense"
}

/**
 * Compose navigation graph wiring the Dashboard and Quick-Expense destinations.
 * History, Reports and Settings destinations follow the same pattern and are
 * added in the next iteration.
 */
@Composable
fun AppNavigation(
    dashboardViewModel: DashboardViewModel,
    quickEntryViewModel: QuickEntryViewModel,
    defaultAccountId: Long,
) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.DASHBOARD) {
        composable(Routes.DASHBOARD) { DashboardScreen(dashboardViewModel) }
        composable(Routes.QUICK_EXPENSE) { QuickExpenseScreen(quickEntryViewModel, defaultAccountId) }
    }
}
