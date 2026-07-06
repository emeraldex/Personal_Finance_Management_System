package com.example.expensedesktop.controller;

import com.example.expensecore.dto.IncomeDTO;
import com.example.expensecore.service.IncomeService;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class IncomeController {

    @FXML
    private TextField dateField;
    @FXML
    private TextField descriptionField;
    @FXML
    private TextField amountField;

    private final IncomeService incomeService;

    public IncomeController() {
        this.incomeService = new IncomeService(new com.example.expensecore.repository.JdbcIncomeRepository(null));
    }

    @FXML
    private void addIncome() {
        String date = dateField.getText();
        String description = descriptionField.getText();
        double amount = Double.parseDouble(amountField.getText());

        IncomeDTO incomeDTO = new IncomeDTO(0, date, description, amount, 1, 1, 1);
        incomeService.addIncome(incomeDTO);

        dateField.clear();
        descriptionField.clear();
        amountField.clear();
    }
}
