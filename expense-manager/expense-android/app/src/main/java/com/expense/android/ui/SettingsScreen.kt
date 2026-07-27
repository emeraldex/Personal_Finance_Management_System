package com.expense.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.math.BigDecimal
import java.util.Currency

/**
 * Settings / About screen: budget-alert toggle, fixed exchange-rate management
 * (backing the multi-currency expense entry), and read-only environment details
 * for this local-first build. The app stores everything on-device and reuses the
 * shared `expense-core` for all rules, computation and validation.
 */
@Composable
fun SettingsScreen(
    currencyCode: String,
    storagePath: String,
    budgetAlertsEnabled: Boolean,
    onBudgetAlertsChange: (Boolean) -> Unit,
    fxRates: Map<String, BigDecimal>,
    onSetFxRate: (String, BigDecimal) -> Unit,
    onRemoveFxRate: (String) -> Unit,
) {
    var alertsOn by remember { mutableStateOf(budgetAlertsEnabled) }
    var rates by remember { mutableStateOf(fxRates) }
    var fxCode by remember { mutableStateOf("") }
    var fxRate by remember { mutableStateOf("") }
    var fxStatus by remember { mutableStateOf("") }
    Column(
        Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)

        Card(Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Budget alerts", style = MaterialTheme.typography.labelMedium)
                    Text(
                        "Notify when a category approaches or exceeds its monthly budget",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Switch(
                    checked = alertsOn,
                    onCheckedChange = {
                        alertsOn = it
                        onBudgetAlertsChange(it)
                    },
                )
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Exchange rates", style = MaterialTheme.typography.labelMedium)
                Text(
                    "1 <code> = <rate> $currencyCode — lets Add Expense take amounts in that " +
                        "currency and convert them on save.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = fxCode,
                        onValueChange = { fxCode = it },
                        label = { Text("Code") },
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = fxRate,
                        onValueChange = { fxRate = it },
                        label = { Text("Rate") },
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        val code = fxCode.trim().uppercase()
                        val rate = fxRate.trim().toBigDecimalOrNull()
                        fxStatus = when {
                            !isValidCurrency(code) -> "Enter a valid ISO currency code (e.g. USD)"
                            code == currencyCode -> "Rates are relative to $currencyCode — enter a foreign currency"
                            rate == null || rate.signum() <= 0 -> "Rate must be a positive number"
                            else -> {
                                onSetFxRate(code, rate)
                                rates = (rates + (code to rate)).toSortedMap()
                                "Rate saved: 1 $code = ${rate.toPlainString()} $currencyCode"
                            }
                        }
                    }) { Text("Set rate") }
                    OutlinedButton(onClick = {
                        val code = fxCode.trim().uppercase()
                        fxStatus = if (!isValidCurrency(code)) {
                            "Enter a valid ISO currency code (e.g. USD)"
                        } else {
                            onRemoveFxRate(code)
                            rates = rates - code
                            "Rate removed for $code"
                        }
                    }) { Text("Remove") }
                }
                Text(
                    if (rates.isEmpty()) "No rates yet"
                    else rates.entries.joinToString("\n") {
                        "1 ${it.key} = ${it.value.toPlainString()} $currencyCode"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (fxStatus.isNotEmpty()) {
                    Text(fxStatus, style = MaterialTheme.typography.bodySmall)
                }
            }
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

private fun isValidCurrency(code: String): Boolean =
    try {
        Currency.getInstance(code)
        true
    } catch (e: IllegalArgumentException) {
        false
    }

private fun String.toBigDecimalOrNull(): BigDecimal? =
    try {
        BigDecimal(this)
    } catch (e: NumberFormatException) {
        null
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
