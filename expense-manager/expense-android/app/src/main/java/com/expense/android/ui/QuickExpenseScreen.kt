package com.expense.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.expense.android.viewmodel.QuickEntryViewModel

/**
 * Quick Expense entry. Delegates to the shared core through [QuickEntryViewModel]
 * and the reusable [EntryForm]; [accountId] is the user's default account.
 */
@Composable
fun QuickExpenseScreen(viewModel: QuickEntryViewModel, accountId: Long) {
    val status by viewModel.status.collectAsState()
    val options by viewModel.options.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadOptions() }

    EntryForm(
        title = "Add expense",
        accounts = options.accounts,
        categories = options.expenseCategories,
        defaultAccountId = accountId,
        status = status,
        submitLabel = "Save expense",
        currencyOptions = options.entryCurrencyCodes,
        onSubmit = { acct, category, amount, description, currencyCode ->
            viewModel.addExpense(acct, category, amount, description, currencyCode)
        },
    )
}
