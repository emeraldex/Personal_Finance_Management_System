package com.expense.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.expense.android.ui.components.PickerField
import com.expense.android.viewmodel.ManageViewModel
import com.expense.core.domain.AccountType
import com.expense.core.domain.Category
import com.expense.core.domain.CategoryType
import com.expense.core.domain.PaymentMethodType

/**
 * Manage screen: the Android counterpart of the desktop Manage view. Creates and
 * archives accounts and payment methods, and creates, renames and archives
 * categories. Archiving hides an entry from the entry pickers while every
 * historical transaction that references it keeps its label.
 */
@Composable
fun ManageScreen(viewModel: ManageViewModel) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.load() }

    var renaming by remember { mutableStateOf<Category?>(null) }

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Manage", style = MaterialTheme.typography.headlineSmall)
        if (state.status.isNotEmpty()) {
            Text(state.status, style = MaterialTheme.typography.bodySmall)
        }

        // ---- Accounts -------------------------------------------------------
        SectionHeader("Accounts")
        var accountName by remember { mutableStateOf("") }
        var accountOpening by remember { mutableStateOf("") }
        var accountType by remember { mutableStateOf(AccountType.CASH) }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = accountName,
                onValueChange = { accountName = it },
                label = { Text("New account name") },
                modifier = Modifier.fillMaxWidth(),
            )
            PickerField(
                label = "Type",
                options = remember { AccountType.values().toList() },
                selected = accountType,
                itemLabel = { it.name },
                onSelect = { accountType = it },
            )
            OutlinedTextField(
                value = accountOpening,
                onValueChange = { accountOpening = it },
                label = { Text("Opening balance (optional)") },
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = {
                    viewModel.createAccount(accountName, accountType, accountOpening)
                    accountName = ""
                    accountOpening = ""
                },
                enabled = accountName.isNotBlank(),
            ) {
                Text("Add account")
            }
        }
        state.accounts.forEach { account ->
            EntityRow(
                name = account.name(),
                detail = "${account.type().name} • opening ${account.openingBalance()}",
                archived = account.archived(),
                onToggleArchived = {
                    viewModel.setAccountArchived(account.id(), !account.archived())
                },
            )
        }

        // ---- Categories -----------------------------------------------------
        SectionHeader("Categories")
        var categoryName by remember { mutableStateOf("") }
        var categoryType by remember { mutableStateOf(CategoryType.EXPENSE) }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                label = { Text("New category name") },
                modifier = Modifier.fillMaxWidth(),
            )
            PickerField(
                label = "Type",
                options = remember { CategoryType.values().toList() },
                selected = categoryType,
                itemLabel = { it.name },
                onSelect = { categoryType = it },
            )
            Button(
                onClick = {
                    viewModel.createCategory(categoryName, categoryType)
                    categoryName = ""
                },
                enabled = categoryName.isNotBlank(),
            ) {
                Text("Add category")
            }
        }
        state.categories.forEach { category ->
            EntityRow(
                name = category.name(),
                detail = category.type().name,
                archived = category.archived(),
                onToggleArchived = {
                    viewModel.setCategoryArchived(category.id(), !category.archived())
                },
                extraAction = Pair("Rename", { renaming = category }),
            )
        }

        // ---- Payment methods ------------------------------------------------
        SectionHeader("Payment methods")
        var paymentName by remember { mutableStateOf("") }
        var paymentType by remember { mutableStateOf(PaymentMethodType.CASH) }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = paymentName,
                onValueChange = { paymentName = it },
                label = { Text("New payment method name") },
                modifier = Modifier.fillMaxWidth(),
            )
            PickerField(
                label = "Type",
                options = remember { PaymentMethodType.values().toList() },
                selected = paymentType,
                itemLabel = { it.name },
                onSelect = { paymentType = it },
            )
            Button(
                onClick = {
                    viewModel.createPaymentMethod(paymentName, paymentType)
                    paymentName = ""
                },
                enabled = paymentName.isNotBlank(),
            ) {
                Text("Add payment method")
            }
        }
        state.paymentMethods.forEach { method ->
            EntityRow(
                name = method.name(),
                detail = method.type().name,
                archived = method.archived(),
                onToggleArchived = {
                    viewModel.setPaymentMethodArchived(method.id(), !method.archived())
                },
            )
        }
    }

    renaming?.let { category ->
        RenameCategoryDialog(
            current = category.name(),
            onDismiss = { renaming = null },
            onConfirm = { newName ->
                viewModel.renameCategory(category.id(), newName)
                renaming = null
            },
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium)
}

/** One reference-data row with its archive/restore action and optional extra action. */
@Composable
private fun EntityRow(
    name: String,
    detail: String,
    archived: Boolean,
    onToggleArchived: () -> Unit,
    extraAction: Pair<String, () -> Unit>? = null,
) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f).padding(end = 8.dp)) {
                Text(if (archived) "$name (archived)" else name)
                Text(detail, style = MaterialTheme.typography.bodySmall)
            }
            Row {
                extraAction?.let { (label, action) ->
                    TextButton(onClick = action) { Text(label) }
                }
                TextButton(onClick = onToggleArchived) {
                    Text(if (archived) "Restore" else "Archive")
                }
            }
        }
    }
}

@Composable
private fun RenameCategoryDialog(
    current: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember(current) { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename category") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) { Text("Rename") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
