package com.expense.android.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.expense.android.viewmodel.QuickEntryViewModel

/**
 * Quick Expense entry. A minimal amount+description form that delegates to the
 * shared core through [QuickEntryViewModel]. [accountId] is supplied by the host
 * (e.g. the user's default account).
 */
@Composable
fun QuickExpenseScreen(viewModel: QuickEntryViewModel, accountId: Long) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val status by viewModel.status.collectAsState()

    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Text("Add expense")
        OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
        Button(onClick = { viewModel.addExpense(accountId, null, amount, description) }) {
            Text("Save")
        }
        if (status.isNotEmpty()) {
            Text(status, Modifier.padding(top = 8.dp))
        }
    }
}
