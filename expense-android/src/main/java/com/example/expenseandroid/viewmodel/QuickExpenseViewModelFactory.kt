package com.example.expenseandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class QuickExpenseViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuickExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuickExpenseViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
