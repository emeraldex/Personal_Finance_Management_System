package com.example.expenseandroid.viewmodel

import androidx.lifecycle.ViewModel
import com.example.expensecore.dto.ExpenseDTO
import com.example.expensecore.service.ExpenseService
import com.example.expensecore.repository.JdbcExpenseRepository

class ExpenseViewModel : ViewModel() {
    private val expenseService = ExpenseService(JdbcExpenseRepository(null))

    fun addExpense(expenseDTO: ExpenseDTO) {
        expenseService.addExpense(expenseDTO)
    }
}
