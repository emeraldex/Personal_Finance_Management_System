package com.expense.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.expense.android.ui.components.PickerField
import com.expense.core.domain.Account
import com.expense.core.domain.Category

/**
 * Shared amount + account + category + description form used by both the
 * Quick Expense and Quick Income screens. Presentation only; the submit action
 * is provided by the host and delegates to the ViewModel.
 */
@Composable
fun EntryForm(
    title: String,
    accounts: List<Account>,
    categories: List<Category>,
    defaultAccountId: Long,
    status: String,
    submitLabel: String,
    onSubmit: (accountId: Long, categoryId: Long?, amount: String, description: String) -> Unit,
) {
    var selectedAccount by remember(accounts) {
        mutableStateOf(accounts.firstOrNull { it.id() == defaultAccountId } ?: accounts.firstOrNull())
    }
    var selectedCategory by remember(categories) { mutableStateOf<Category?>(null) }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall)

        PickerField(
            label = "Account",
            options = accounts,
            selected = selectedAccount,
            itemLabel = { it.name() },
            onSelect = { selectedAccount = it },
        )
        PickerField(
            label = "Category (optional)",
            options = categories,
            selected = selectedCategory,
            itemLabel = { it.name() },
            onSelect = { selectedCategory = it },
        )
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = {
                selectedAccount?.let { acct ->
                    onSubmit(acct.id(), selectedCategory?.id(), amount, description)
                    amount = ""
                    description = ""
                }
            },
            enabled = selectedAccount != null,
        ) {
            Text(submitLabel)
        }
        if (status.isNotEmpty()) {
            Text(status)
        }
    }
}
