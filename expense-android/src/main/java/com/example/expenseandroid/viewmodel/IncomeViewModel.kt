package com.example.expenseandroid.viewmodel

import androidx.lifecycle.ViewModel
import com.example.expensecore.dto.IncomeDTO
import com.example.expensecore.service.IncomeService
import com.example.expensecore.repository.JdbcIncomeRepository

class IncomeViewModel : ViewModel() {
    private val incomeService = IncomeService(JdbcIncomeRepository(null))

    fun addIncome(incomeDTO: IncomeDTO) {
        incomeService.addIncome(incomeDTO)
    }
}
