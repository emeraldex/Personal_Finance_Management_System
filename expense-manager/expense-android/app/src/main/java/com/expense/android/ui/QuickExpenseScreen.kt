package com.expense.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.expense.android.viewmodel.QuickEntryViewModel

/**
 * Quick Expense entry. Delegates to the shared core through [QuickEntryViewModel]
 * and the reusable [EntryForm]; [accountId] is the user's default account.
 *
 * Expenses additionally carry a payment method and a category suggested by the
 * core's offline categoriser as the description is typed.
 */
@Composable
fun QuickExpenseScreen(viewModel: QuickEntryViewModel, accountId: Long) {
    val status by viewModel.status.collectAsState()
    val options by viewModel.options.collectAsState()
    val hint by viewModel.hint.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadOptions() }

    EntryForm(
        title = "Add expense",
        accounts = options.accounts,
        categories = options.expenseCategories,
        paymentMethods = options.paymentMethods,
        defaultAccountId = accountId,
        status = status,
        submitLabel = "Save expense",
        hint = hint,
        onDescriptionChanged = viewModel::suggestCategory,
        onSubmit = { acct, category, paymentMethod, amount, description, date ->
            viewModel.addExpense(acct, category, paymentMethod, amount, description, date)
        },
    )
}
