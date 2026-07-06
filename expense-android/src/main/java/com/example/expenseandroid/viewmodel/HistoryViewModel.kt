package com.example.expenseandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModelScope
import com.example.expensecore.dto.ExpenseDTO
import com.example.expensecore.dto.IncomeDTO
import com.example.expensecore.service.ExpenseService
import com.example.expensecore.service.IncomeService
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HistoryViewModel : ViewModel() {
    private val expenseService = ExpenseService(com.example.expensecore.repository.JdbcExpenseRepository(null))
    private val incomeService = IncomeService(com.example.expensecore.repository.JdbcIncomeRepository(null))

    private val _expenses = MutableStateFlow(emptyList<ExpenseDTO>())
    val expenses: StateFlow<List<ExpenseDTO>> get() = _expenses

    private val _incomes = MutableStateFlow(emptyList<IncomeDTO>())
    val incomes: StateFlow<List<IncomeDTO>> get() = _incomes

    fun loadHistory() {
        viewModelScope.launch {
            _expenses.value = expenseService.getAllExpenses()
            _incomes.value = incomeService.getAllIncomes()
        }
    }
}
