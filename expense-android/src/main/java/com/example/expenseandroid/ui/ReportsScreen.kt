package com.example.expenseandroid.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expenseandroid.viewmodel.ReportsViewModel
import com.example.expenseandroid.viewmodel.ReportsViewModelFactory

class ReportsScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReportsScreen()
        }
    }
}

@Composable
fun ReportsScreen() {
    val reportsViewModel: ReportsViewModel = viewModel(factory = ReportsViewModelFactory())
    val totalIncome by reportsViewModel.totalIncome.collectAsState()
    val totalExpenses by reportsViewModel.totalExpenses.collectAsState()
    val netBalance by reportsViewModel.netBalance.collectAsState()

    Scaffold {
        Column {
            Text("Total Income: $totalIncome")
            Text("Total Expenses: $totalExpenses")
            Text("Net Balance: $netBalance")
            Button(onClick = {
                reportsViewModel.updateReports()
            }) {
                Text("Refresh")
            }
            Button(onClick = {
                reportsViewModel.exportToExcel()
            }) {
                Text("Export to Excel")
            }
            Button(onClick = {
                reportsViewModel.exportToCsv()
            }) {
                Text("Export to CSV")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ReportsScreen()
}
