package com.example.expenseandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class IncomeViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IncomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return IncomeViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
