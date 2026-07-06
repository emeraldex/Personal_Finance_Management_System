package com.example.expensecore.service;

import com.example.expensecore.dto.ExpenseDTO;
import com.example.expensecore.repository.ExpenseRepository;
import java.util.List;

public class ExpenseService {
    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public List<ExpenseDTO> getAllExpenses() {
        return expenseRepository.getAllExpenses();
    }

    public ExpenseDTO getExpenseById(int id) {
        return expenseRepository.getExpenseById(id);
    }

    public void addExpense(ExpenseDTO expenseDTO) {
        expenseRepository.addExpense(expenseDTO);
    }

    public void updateExpense(ExpenseDTO expenseDTO) {
        expenseRepository.updateExpense(expenseDTO);
    }

    public void deleteExpense(int id) {
        expenseRepository.deleteExpense(id);
    }
}
