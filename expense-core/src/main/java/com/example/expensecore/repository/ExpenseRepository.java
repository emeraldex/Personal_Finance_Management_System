package com.example.expensecore.repository;

import com.example.expensecore.domain.Expense;
import com.example.expensecore.dto.ExpenseDTO;
import java.util.List;

public interface ExpenseRepository {
    List<Expense> getAllExpenses();
    Expense getExpenseById(int id);
    void addExpense(ExpenseDTO expenseDTO);
    void updateExpense(ExpenseDTO expenseDTO);
    void deleteExpense(int id);
}
