package com.expense.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.expense.android.ui.components.MonthPager
import com.expense.android.viewmodel.DashboardViewModel

/**
 * Dashboard screen. Collects [DashboardViewModel.state] and renders headline
 * cards plus a category breakdown list. Presentation only — no business logic.
 */
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.load() }

    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("${state.month} overview", style = MaterialTheme.typography.headlineSmall)
        MonthPager(state.month, onPrev = viewModel::prevMonth, onNext = viewModel::nextMonth)

        if (state.loading) {
            CircularProgressIndicator()
        }

        state.summary?.let { s ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard("Income", s.totalIncome().toString(), Modifier.weight(1f))
                MetricCard("Expenses", s.totalExpense().toString(), Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard("Net", s.netBalance().toString(), Modifier.weight(1f))
                MetricCard("Savings", s.savings().toString(), Modifier.weight(1f))
                MetricCard("Outstanding", s.outstanding().toString(), Modifier.weight(1f))
            }

            Text("Spending by category", style = MaterialTheme.typography.titleMedium)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(s.categoryBreakdown()) { item ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(item.categoryName())
                            Text("${item.total()}  (${item.percentage()}%)")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}
