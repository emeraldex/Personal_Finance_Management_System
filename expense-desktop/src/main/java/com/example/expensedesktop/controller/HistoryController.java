package com.example.expensedesktop.controller;

import com.example.expensecore.dto.ExpenseDTO;
import com.example.expensecore.dto.IncomeDTO;
import com.example.expensecore.service.ExpenseService;
import com.example.expensecore.service.IncomeService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.util.List;

public class HistoryController {

    @FXML
    private Label expensesLabel;
    @FXML
    private Label incomesLabel;

    private final ExpenseService expenseService;
    private final IncomeService incomeService;

    public HistoryController() {
        this.expenseService = new ExpenseService(new com.example.expensecore.repository.JdbcExpenseRepository(null));
        this.incomeService = new IncomeService(new com.example.expensecore.repository.JdbcIncomeRepository(null));
    }

    @FXML
    private void initialize() {
        refreshHistory();
    }

    private void refreshHistory() {
        List<ExpenseDTO> expenses = expenseService.getAllExpenses();
        StringBuilder expensesText = new StringBuilder();
        for (ExpenseDTO expense : expenses) {
            expensesText.append(expense.getDate()).append(" - ").append(expense.getDescription()).append(" - ").append(expense.getAmount()).append("\n");
        }
        expensesLabel.setText(expensesText.toString());

        List<IncomeDTO> incomes = incomeService.getAllIncomes();
        StringBuilder incomesText = new StringBuilder();
        for (IncomeDTO income : incomes) {
            incomesText.append(income.getDate()).append(" - ").append(income.getDescription()).append(" - ").append(income.getAmount()).append("\n");
        }
        incomesLabel.setText(incomesText.toString());
    }

    @FXML
    private void refreshHistory() {
        refreshHistory();
    }
}
