package com.expense.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Settings / About screen. Read-only environment details for this local-first
 * build: the app stores everything on-device and reuses the shared `expense-core`
 * for all rules, computation and validation.
 */
@Composable
fun SettingsScreen(currencyCode: String, storagePath: String) {
    Column(
        Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)

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
