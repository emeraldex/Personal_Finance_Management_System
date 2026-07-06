package com.example.expensedesktop.controller;

import com.example.expensecore.dto.ExpenseDTO;
import com.example.expensecore.dto.IncomeDTO;
import com.example.expensecore.service.ExpenseService;
import com.example.expensecore.service.IncomeService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.util.List;

public class ReportsController {

    @FXML
    private Label totalIncomeLabel;
    @FXML
    private Label totalExpensesLabel;
    @FXML
    private Label netBalanceLabel;

    private final ExpenseService expenseService;
    private final IncomeService incomeService;

    public ReportsController() {
        this.expenseService = new ExpenseService(new com.example.expensecore.repository.JdbcExpenseRepository(null));
        this.incomeService = new IncomeService(new com.example.expensecore.repository.JdbcIncomeRepository(null));
    }

    @FXML
    private void initialize() {
        updateReports();
    }

    private void updateReports() {
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
    private void exportToExcel() {
        // Implement export to Excel functionality
    }

    @FXML
    private void exportToCsv() {
        // Implement export to CSV functionality
    }
}
