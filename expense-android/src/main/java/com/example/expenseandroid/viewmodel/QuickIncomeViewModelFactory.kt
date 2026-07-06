package com.example.expenseandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class QuickIncomeViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuickIncomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuickIncomeViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
