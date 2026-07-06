package com.example.expensedesktop.controller;

import com.example.expensecore.dto.ExpenseDTO;
import com.example.expensecore.dto.IncomeDTO;
import com.example.expensecore.service.ExpenseService;
import com.example.expensecore.service.IncomeService;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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
        this.expenseService = new ExpenseService(
            new com.example.expensecore.repository.JdbcExpenseRepository(null)
        );
        this.incomeService = new IncomeService(
            new com.example.expensecore.repository.JdbcIncomeRepository(null)
        );
    }

    @FXML
    private void initialize() {
        updateReports();
    }

    private void updateReports() {
        List<ExpenseDTO> expenses = expenseService.getAllExpenses();
        double totalExpenses = expenses
            .stream()
            .mapToDouble(ExpenseDTO::getAmount)
            .sum();
        totalExpensesLabel.setText(String.valueOf(totalExpenses));

        List<IncomeDTO> incomes = incomeService.getAllIncomes();
        double totalIncome = incomes
            .stream()
            .mapToDouble(IncomeDTO::getAmount)
            .sum();
        totalIncomeLabel.setText(String.valueOf(totalIncome));

        double netBalance = totalIncome + totalExpenses;
        netBalanceLabel.setText(String.valueOf(netBalance));
    }

    @FXML
    private void exportToExcel() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Reports");
            Row headerRow = sheet.createRow(0);
            Cell headerCell = headerRow.createCell(0);
            headerCell.setCellValue("Date");
            headerCell = headerRow.createCell(1);
            headerCell.setCellValue("Description");
            headerCell = headerRow.createCell(2);
            headerCell.setCellValue("Amount");

            int rowNum = 1;
            for (ExpenseDTO expense : expenseService.getAllExpenses()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(expense.getDate());
                row.createCell(1).setCellValue(expense.getDescription());
                row.createCell(2).setCellValue(expense.getAmount());
            }
            for (IncomeDTO income : incomeService.getAllIncomes()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(income.getDate());
                row.createCell(1).setCellValue(income.getDescription());
                row.createCell(2).setCellValue(income.getAmount());
            }

            try (
                FileOutputStream fileOut = new FileOutputStream("reports.xlsx")
            ) {
                workbook.write(fileOut);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void exportToCsv() {
        try (
            java.io.FileWriter writer = new java.io.FileWriter("reports.csv")
        ) {
            writer.append("Date,Description,Amount\n");
            for (ExpenseDTO expense : expenseService.getAllExpenses()) {
                writer.append(expense.getDate()).append(",");
                writer.append(expense.getDescription()).append(",");
                writer.append(String.valueOf(expense.getAmount())).append("\n");
            }
            for (IncomeDTO income : incomeService.getAllIncomes()) {
                writer.append(income.getDate()).append(",");
                writer.append(income.getDescription()).append(",");
                writer.append(String.valueOf(income.getAmount())).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
