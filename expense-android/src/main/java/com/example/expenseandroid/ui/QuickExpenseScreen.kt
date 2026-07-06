package com.example.expenseandroid.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.example.expenseandroid.viewmodel.QuickExpenseViewModel
import com.example.expenseandroid.viewmodel.QuickExpenseViewModelFactory
import com.example.expensecore.dto.ExpenseDTO
import com.example.expensecore.service.ExpenseService

class QuickExpenseScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuickExpenseScreen()
        }
    }
}

@Composable
fun QuickExpenseScreen() {
    val quickExpenseViewModel: QuickExpenseViewModel = viewModel(factory = QuickExpenseViewModelFactory())
    val date = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    val amount = remember { mutableStateOf("") }

    Scaffold {
        Column {
            Text("Date")
            TextField(value = date.value, onValueChange = { date.value = it })
            Text("Description")
            TextField(value = description.value, onValueChange = { description.value = it })
            Text("Amount")
            TextField(value = amount.value, onValueChange = { amount.value = it })
            Button(onClick = {
                val expenseDTO = ExpenseDTO(
                    id = 0,
                    date = date.value,
                    description = description.value,
                    amount = amount.value.toDouble(),
                    categoryId = 1,
                    paymentMethodId = 1,
                    accountId = 1
                )
                quickExpenseViewModel.addQuickExpense(expenseDTO)
                date.value = ""
                description.value = ""
                amount.value = ""
            }) {
                Text("Add Quick Expense")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    QuickExpenseScreen()
}
