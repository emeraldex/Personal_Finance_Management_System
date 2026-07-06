package com.example.expenseandroid.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.example.expenseandroid.viewmodel.QuickIncomeViewModel
import com.example.expenseandroid.viewmodel.QuickIncomeViewModelFactory
import com.example.expensecore.dto.IncomeDTO
import com.example.expensecore.service.IncomeService

class QuickIncomeScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuickIncomeScreen()
        }
    }
}

@Composable
fun QuickIncomeScreen() {
    val quickIncomeViewModel: QuickIncomeViewModel = viewModel(factory = QuickIncomeViewModelFactory())
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
                val incomeDTO = IncomeDTO(
                    id = 0,
                    date = date.value,
                    description = description.value,
                    amount = amount.value.toDouble(),
                    categoryId = 1,
                    paymentMethodId = 1,
                    accountId = 1
                )
                quickIncomeViewModel.addQuickIncome(incomeDTO)
                date.value = ""
                description.value = ""
                amount.value = ""
            }) {
                Text("Add Quick Income")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    QuickIncomeScreen()
}
