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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.expense.android.ui.components.MonthPager
import com.expense.android.ui.components.PickerField
import com.expense.android.viewmodel.ReportsViewModel
import com.expense.core.domain.Category

/**
 * Reports screen: the rich monthly breakdowns computed by the shared core
 * (payment methods, budget utilisation, cash flow) plus a form to set a
 * per-category monthly budget.
 */
@Composable
fun ReportsScreen(viewModel: ReportsViewModel) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.load() }

    Column(
        Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Reports", style = MaterialTheme.typography.headlineSmall)
        MonthPager(state.month, onPrev = viewModel::prevMonth, onNext = viewModel::nextMonth)

        if (state.loading) {
            CircularProgressIndicator()
        }

        state.summary?.let { s ->
            SetBudgetForm(state.expenseCategories, state.status) { categoryId, limit ->
                viewModel.setBudget(categoryId, limit)
            }

            Section("Budgets") {
                if (s.budgetUtilization().isEmpty()) {
                    Text("No budgets set.", style = MaterialTheme.typography.bodySmall)
                }
                s.budgetUtilization().forEach { b ->
                    LabelledRow(
                        b.categoryName(),
                        "${b.spent()} / ${b.limit()}  (${b.utilizationPct()}%)"
                            + if (b.overBudget()) "  OVER" else "",
                    )
                }
            }

            Section("Spending by payment method") {
                if (s.paymentBreakdown().isEmpty()) {
                    Text("No spending this month.", style = MaterialTheme.typography.bodySmall)
                }
                s.paymentBreakdown().forEach { p ->
                    LabelledRow(p.name(), "${p.total()}  (${p.percentage()}%)")
                }
            }

            Section("Daily cash flow") {
                if (s.cashFlow().isEmpty()) {
                    Text("No activity this month.", style = MaterialTheme.typography.bodySmall)
                }
                s.cashFlow().forEach { point ->
                    LabelledRow(point.date().toString(), "net ${point.net()}")
                }
            }
        }
    }
}

@Composable
private fun SetBudgetForm(
    categories: List<Category>,
    status: String,
    onSet: (categoryId: Long, limit: String) -> Unit,
) {
    var selected by remember(categories) { mutableStateOf<Category?>(null) }
    var limit by remember { mutableStateOf("") }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Set a monthly budget", style = MaterialTheme.typography.titleMedium)
            PickerField(
                label = "Category",
                options = categories,
                selected = selected,
                itemLabel = { it.name() },
                onSelect = { selected = it },
            )
            OutlinedTextField(
                value = limit,
                onValueChange = { limit = it },
                label = { Text("Monthly cap") },
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = { selected?.let { onSet(it.id(), limit); limit = "" } },
                enabled = selected != null,
            ) {
                Text("Save budget")
            }
            if (status.isNotEmpty()) {
                Text(status, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun LabelledRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
