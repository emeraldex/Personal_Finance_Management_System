package com.example.expensedesktop.controller;

import com.example.expensecore.dto.ExpenseDTO;
import com.example.expensecore.service.ExpenseService;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class ExpenseController {

    @FXML
    private TextField dateField;
    @FXML
    private TextField descriptionField;
    @FXML
    private TextField amountField;

    private final ExpenseService expenseService;

    public ExpenseController() {
        this.expenseService = new ExpenseService(new com.example.expensecore.repository.JdbcExpenseRepository(null));
    }

    @FXML
    private void addExpense() {
        String date = dateField.getText();
        String description = descriptionField.getText();
        double amount = Double.parseDouble(amountField.getText());

        ExpenseDTO expenseDTO = new ExpenseDTO(0, date, description, amount, 1, 1, 1);
        expenseService.addExpense(expenseDTO);

        dateField.clear();
        descriptionField.clear();
        amountField.clear();
    }
}
