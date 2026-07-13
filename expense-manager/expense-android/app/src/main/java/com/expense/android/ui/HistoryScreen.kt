package com.expense.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.expense.android.ui.components.MonthPager
import com.expense.android.viewmodel.HistoryRow
import com.expense.android.viewmodel.HistoryViewModel

/**
 * History screen: a month pager and the month's transactions. Each row has a
 * Delete action that dispatches through the shared core via [HistoryViewModel].
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

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(state.rows) { row -> HistoryItem(row, onDelete = { viewModel.delete(row.transaction) }) }
        }
    }
}

@Composable
private fun HistoryItem(row: HistoryRow, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("${row.date}  •  ${row.type}", style = MaterialTheme.typography.labelMedium)
            Text(row.amount, style = MaterialTheme.typography.titleMedium)
            Text(
                "${row.category}  •  ${row.account}",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            TextButton(onClick = onDelete, modifier = Modifier.align(Alignment.End)) { Text("Delete") }
        }
    }
}
