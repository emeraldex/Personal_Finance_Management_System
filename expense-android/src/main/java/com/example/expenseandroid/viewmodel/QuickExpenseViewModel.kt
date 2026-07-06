package com.example.expenseandroid.viewmodel

import androidx.lifecycle.ViewModel
import com.example.expensecore.dto.ExpenseDTO
import com.example.expensecore.service.ExpenseService
import com.example.expensecore.repository.JdbcExpenseRepository

class QuickExpenseViewModel : ViewModel() {
    private val expenseService = ExpenseService(JdbcExpenseRepository(null))

    fun addQuickExpense(expenseDTO: ExpenseDTO) {
        expenseService.addExpense(expenseDTO)
    }
}
