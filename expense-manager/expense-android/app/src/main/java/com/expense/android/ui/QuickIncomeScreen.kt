package com.expense.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.expense.android.viewmodel.QuickEntryViewModel

/**
 * Quick Income entry. Mirrors [QuickExpenseScreen] but targets income categories
 * and the income service via the shared [QuickEntryViewModel] and [EntryForm].
 * Income has no payment method and no categoriser suggestion.
 */
@Composable
fun QuickIncomeScreen(viewModel: QuickEntryViewModel, accountId: Long) {
    val status by viewModel.status.collectAsState()
    val options by viewModel.options.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadOptions() }

    EntryForm(
        title = "Add income",
        accounts = options.accounts,
        categories = options.incomeCategories,
        paymentMethods = emptyList(),
        defaultAccountId = accountId,
        status = status,
        submitLabel = "Save income",
        hint = null,
        onDescriptionChanged = {},
        onSubmit = { acct, category, _, amount, description, date ->
            viewModel.addIncome(acct, category, amount, description, date)
        },
    )
}
