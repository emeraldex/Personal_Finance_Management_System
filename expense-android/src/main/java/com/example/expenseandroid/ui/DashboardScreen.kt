package com.example.expenseandroid.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expenseandroid.viewmodel.DashboardViewModel
import com.example.expenseandroid.viewmodel.DashboardViewModelFactory

class DashboardScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DashboardScreen()
        }
    }
}

@Composable
fun DashboardScreen() {
    val dashboardViewModel: DashboardViewModel = viewModel(factory = DashboardViewModelFactory())
    val totalIncome by dashboardViewModel.totalIncome.collectAsState()
    val totalExpenses by dashboardViewModel.totalExpenses.collectAsState()
    val netBalance by dashboardViewModel.netBalance.collectAsState()

    Scaffold {
        Column {
            Text("Total Income: $totalIncome")
            Text("Total Expenses: $totalExpenses")
            Text("Net Balance: $netBalance")
            Button(onClick = {
                dashboardViewModel.updateDashboard()
            }) {
                Text("Refresh")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DashboardScreen()
}
