package com.example.expensecore.service;

import com.example.expensecore.dto.ExpenseDTO;
import com.example.expensecore.service.ExpenseService;

public class OCRDataHandler {
    private final ExpenseService expenseService;

    public OCRDataHandler(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    public void handleExtractedData(String extractedText) {
        // Implement logic to parse extractedText and create ExpenseDTO objects
        // Example: Extract date, description, and amount from the text
        // ExpenseDTO expense = new ExpenseDTO(..);
        // expenseService.addExpense(expense);
    }
}
