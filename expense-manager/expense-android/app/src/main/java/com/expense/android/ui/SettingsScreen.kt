package com.expense.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.expense.android.ui.components.MonthPager
import com.expense.android.viewmodel.SettingsViewModel

/**
 * Settings screen: environment details plus CSV export. Exports are rendered by
 * the shared core exporters (the same ones the desktop uses) and handed to the
 * Android share sheet, so the user can send a month's data anywhere without the
 * app needing storage permissions.
 *
 * Excel and PDF export stay desktop-only: they depend on Apache POI and PDFBox,
 * which are JVM-desktop libraries. CSV is pure Java and runs unchanged here.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    currencyCode: String,
    storagePath: String,
    onManage: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // The ViewModel renders the CSV; the screen owns the Android share plumbing.
    LaunchedEffect(state.pendingExport) {
        state.pendingExport?.let { payload ->
            shareCsv(context, payload)
            viewModel.exportConsumed()
        }
    }

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)

        Text("Manage data", style = MaterialTheme.typography.titleMedium)
        OutlinedButton(onClick = onManage) { Text("Accounts, categories & payment methods") }

        Text("Export", style = MaterialTheme.typography.titleMedium)
        MonthPager(state.month, onPrev = viewModel::prevMonth, onNext = viewModel::nextMonth)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = viewModel::exportTransactions, enabled = !state.exporting) {
                Text("Transactions CSV")
            }
            Button(onClick = viewModel::exportSummary, enabled = !state.exporting) {
                Text("Summary CSV")
            }
        }
        if (state.exporting) {
            CircularProgressIndicator()
        }
        if (state.status.isNotEmpty()) {
            Text(state.status, style = MaterialTheme.typography.bodySmall)
        }

        InfoCard("Currency", currencyCode)
        InfoCard("Data location", storagePath)
        InfoCard("Version", "1.0.0")
        InfoCard(
            "About",
            "Personal Finance & Monthly Expense Management System. All data is stored "
                + "locally on this device; the app shares its business logic, analytics and "
                + "validation with the desktop build through the pure-Java expense-core library.",
        )
    }
}

@Composable
private fun InfoCard(label: String, value: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
