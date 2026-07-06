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

class DashboardViewModel : ViewModel() {
    private val expenseService = ExpenseService(com.example.expensecore.repository.JdbcExpenseRepository(null))
    private val incomeService = IncomeService(com.example.expensecore.repository.JdbcIncomeRepository(null))

    private val _totalIncome = MutableStateFlow(0.0)
    val totalIncome: StateFlow<Double> get() = _totalIncome

    private val _totalExpenses = MutableStateFlow(0.0)
    val totalExpenses: StateFlow<Double> get() = _totalExpenses

    private val _netBalance = MutableStateFlow(0.0)
    val netBalance: StateFlow<Double> get() = _netBalance

    fun updateDashboard() {
        viewModelScope.launch {
            val expenses = expenseService.getAllExpenses()
            val totalExpenses = expenses.sumOf { it.amount }
            _totalExpenses.value = totalExpenses

            val incomes = incomeService.getAllIncomes()
            val totalIncome = incomes.sumOf { it.amount }
            _totalIncome.value = totalIncome

            _netBalance.value = totalIncome + totalExpenses
        }
    }
}
