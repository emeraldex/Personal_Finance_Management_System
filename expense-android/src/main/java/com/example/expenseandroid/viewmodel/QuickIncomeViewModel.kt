package com.example.expenseandroid.viewmodel

import androidx.lifecycle.ViewModel
import com.example.expensecore.dto.IncomeDTO
import com.example.expensecore.service.IncomeService
import com.example.expensecore.repository.JdbcIncomeRepository

class QuickIncomeViewModel : ViewModel() {
    private val incomeService = IncomeService(JdbcIncomeRepository(null))

    fun addQuickIncome(incomeDTO: IncomeDTO) {
        incomeService.addIncome(incomeDTO)
    }
}
