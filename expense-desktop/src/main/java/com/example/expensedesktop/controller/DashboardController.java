package com.example.expensedesktop.controller;

import com.example.expensecore.dto.ExpenseDTO;
import com.example.expensecore.dto.IncomeDTO;
import com.example.expensecore.service.ExpenseService;
import com.example.expensecore.service.IncomeService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.util.List;

public class DashboardController {

    @FXML
    private Label totalIncomeLabel;
    @FXML
    private Label totalExpensesLabel;
    @FXML
    private Label netBalanceLabel;

    private final ExpenseService expenseService;
    private final IncomeService incomeService;

    public DashboardController() {
        this.expenseService = new ExpenseService(new com.example.expensecore.repository.JdbcExpenseRepository(null));
        this.incomeService = new IncomeService(new com.example.expensecore.repository.JdbcIncomeRepository(null));
    }

    @FXML
    private void initialize() {
        updateDashboard();
    }

    private void updateDashboard() {
        List<ExpenseDTO> expenses = expenseService.getAllExpenses();
        double totalExpenses = expenses.stream().mapToDouble(ExpenseDTO::getAmount).sum();
        totalExpensesLabel.setText(String.valueOf(totalExpenses));

        List<IncomeDTO> incomes = incomeService.getAllIncomes();
        double totalIncome = incomes.stream().mapToDouble(IncomeDTO::getAmount).sum();
        totalIncomeLabel.setText(String.valueOf(totalIncome));

        double netBalance = totalIncome + totalExpenses;
        netBalanceLabel.setText(String.valueOf(netBalance));
    }

    @FXML
    private void viewExpenses() {
        // Implement navigation to the expense screen
    }

    @FXML
    private void viewIncomes() {
        // Implement navigation to the income screen
    }
}
