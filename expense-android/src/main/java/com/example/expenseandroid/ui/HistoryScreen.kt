package com.example.expenseandroid.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expenseandroid.viewmodel.HistoryViewModel
import com.example.expenseandroid.viewmodel.HistoryViewModelFactory
import com.example.expensecore.dto.ExpenseDTO
import com.example.expensecore.dto.IncomeDTO
import com.example.expensecore.service.ExpenseService
import com.example.expensecore.service.IncomeService

class HistoryScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HistoryScreen()
        }
    }
}

@Composable
fun HistoryScreen() {
    val historyViewModel: HistoryViewModel = viewModel(factory = HistoryViewModelFactory())
    val expenses by historyViewModel.expenses.collectAsState()
    val incomes by historyViewModel.incomes.collectAsState()

    Scaffold {
        Column {
            Text("Expenses")
            expenses.forEach { expense ->
                Text("${expense.date} - ${expense.description} - ${expense.amount}")
            }
            Text("Incomes")
            incomes.forEach { income ->
                Text("${income.date} - ${income.description} - ${income.amount}")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    HistoryScreen()
}
