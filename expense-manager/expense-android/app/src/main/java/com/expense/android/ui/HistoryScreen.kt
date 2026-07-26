package com.expense.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.expense.android.ui.components.MonthPager
import com.expense.android.ui.components.PickerField
import com.expense.android.viewmodel.HistoryRow
import com.expense.android.viewmodel.HistoryUiState
import com.expense.android.viewmodel.HistoryViewModel
import com.expense.core.domain.Account
import com.expense.core.domain.Category
import com.expense.core.domain.Expense
import com.expense.core.domain.PaymentMethod
import com.expense.core.domain.Transaction
import com.expense.core.domain.TransactionType

/**
 * History screen: a month pager and the month's transactions. Each row can be
 * edited or deleted; both dispatch through the shared core via [HistoryViewModel].
 */
@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.load() }

    Column(
        Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("History", style = MaterialTheme.typography.headlineSmall)
        MonthPager(state.month, onPrev = viewModel::prevMonth, onNext = viewModel::nextMonth)

        if (state.loading) {
            CircularProgressIndicator()
        }
        if (!state.loading && state.rows.isEmpty()) {
            Text("No transactions this month.", style = MaterialTheme.typography.bodyMedium)
        }
        if (state.status.isNotEmpty()) {
            Text(state.status, style = MaterialTheme.typography.bodySmall)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(state.rows) { row ->
                HistoryItem(
                    row = row,
                    onEdit = { viewModel.beginEdit(row.transaction) },
                    onDelete = { viewModel.delete(row.transaction) },
                )
            }
        }
    }

    state.editing?.let { transaction ->
        EditTransactionDialog(
            transaction = transaction,
            state = state,
            onDismiss = viewModel::cancelEdit,
            onSave = { accountId, categoryId, paymentMethodId, amount, description, date ->
                viewModel.saveEdit(
                    transaction, accountId, categoryId, paymentMethodId, amount, description, date,
                )
            },
        )
    }
}

@Composable
private fun HistoryItem(row: HistoryRow, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("${row.date}  •  ${row.type}", style = MaterialTheme.typography.labelMedium)
            Text(row.amount, style = MaterialTheme.typography.titleMedium)
            Text(
                buildString {
                    append(row.category)
                    append("  •  ")
                    append(row.account)
                    row.paymentMethod?.let {
                        append("  •  ")
                        append(it)
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(Modifier.align(Alignment.End)) {
                TextButton(onClick = onEdit) { Text("Edit") }
                TextButton(onClick = onDelete) { Text("Delete") }
            }
        }
    }
}

/**
 * Edit dialog prefilled from the selected transaction. The amount is shown as a
 * positive magnitude; the core reapplies the expense/income sign on save.
 */
@Composable
private fun EditTransactionDialog(
    transaction: Transaction,
    state: HistoryUiState,
    onDismiss: () -> Unit,
    onSave: (
        accountId: Long,
        categoryId: Long?,
        paymentMethodId: Long?,
        amount: String,
        description: String,
        date: String,
    ) -> Unit,
) {
    val isExpense = transaction.type() == TransactionType.EXPENSE
    val categories: List<Category> =
        if (isExpense) state.expenseCategories else state.incomeCategories
    val paymentMethods: List<PaymentMethod> = if (isExpense) state.paymentMethods else emptyList()

    var selectedAccount by remember(transaction, state.accounts) {
        mutableStateOf<Account?>(state.accounts.firstOrNull { it.id() == transaction.accountId() })
    }
    var selectedCategory by remember(transaction, categories) {
        mutableStateOf<Category?>(
            transaction.categoryId()?.let { id -> categories.firstOrNull { it.id() == id } }
        )
    }
    var selectedPaymentMethod by remember(transaction, paymentMethods) {
        mutableStateOf<PaymentMethod?>(
            (transaction as? Expense)?.paymentMethodId()
                ?.let { id -> paymentMethods.firstOrNull { it.id() == id } }
        )
    }
    var amount by remember(transaction) {
        mutableStateOf(transaction.signedAmount().amount().abs().toPlainString())
    }
    var description by remember(transaction) { mutableStateOf(transaction.description()) }
    var date by remember(transaction) { mutableStateOf(transaction.date().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isExpense) "Edit expense" else "Edit income") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PickerField(
                    label = "Account",
                    options = state.accounts,
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
                if (isExpense) {
                    PickerField(
                        label = "Payment method (optional)",
                        options = paymentMethods,
                        selected = selectedPaymentMethod,
                        itemLabel = { it.name() },
                        onSelect = { selectedPaymentMethod = it },
                    )
                }
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
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedAccount?.let { acct ->
                        onSave(
                            acct.id(),
                            selectedCategory?.id(),
                            selectedPaymentMethod?.id(),
                            amount,
                            description,
                            date,
                        )
                    }
                },
                enabled = selectedAccount != null,
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
