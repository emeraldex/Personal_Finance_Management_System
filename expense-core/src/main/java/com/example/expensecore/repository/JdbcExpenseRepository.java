package com.example.expensecore.repository;

import com.example.expensecore.domain.Expense;
import com.example.expensecore.dto.ExpenseDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcExpenseRepository implements ExpenseRepository {

    private final Connection connection;

    public JdbcExpenseRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<Expense> getAllExpenses() {
        List<Expense> expenses = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM Expense")) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Expense expense = new Expense(
                    resultSet.getInt("id"),
                    resultSet.getString("date"),
                    resultSet.getString("description"),
                    resultSet.getDouble("amount"),
                    resultSet.getInt("category_id"),
                    resultSet.getInt("payment_method_id"),
                    resultSet.getInt("account_id")
                );
                expenses.add(expense);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return expenses;
    }

    @Override
    public Expense getExpenseById(int id) {
        Expense expense = null;
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM Expense WHERE id =?")) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                expense = new Expense(
                    resultSet.getInt("id"),
                    resultSet.getString("date"),
                    resultSet.getString("description"),
                    resultSet.getDouble("amount"),
                    resultSet.getInt("category_id"),
                    resultSet.getInt("payment_method_id"),
                    resultSet.getInt("account_id")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return expense;
    }

    @Override
    public void addExpense(ExpenseDTO expenseDTO) {
        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO Expense (date, description, amount, category_id, payment_method_id, account_id) VALUES (?,?,?,?,?,?)")) {
            statement.setString(1, expenseDTO.getDate());
            statement.setString(2, expenseDTO.getDescription());
            statement.setDouble(3, expenseDTO.getAmount());
            statement.setInt(4, expenseDTO.getCategoryId());
            statement.setInt(5, expenseDTO.getPaymentMethodId());
            statement.setInt(6, expenseDTO.getAccountId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateExpense(ExpenseDTO expenseDTO) {
        try (PreparedStatement statement = connection.prepareStatement(
            "UPDATE Expense SET date =?, description =?, amount =?, category_id =?, payment_method_id =?, account_id =? WHERE id =?")) {
            statement.setString(1, expenseDTO.getDate());
            statement.setString(2, expenseDTO.getDescription());
            statement.setDouble(3, expenseDTO.getAmount());
            statement.setInt(4, expenseDTO.getCategoryId());
            statement.setInt(5, expenseDTO.getPaymentMethodId());
            statement.setInt(6, expenseDTO.getAccountId());
            statement.setInt(7, expenseDTO.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteExpense(int id) {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM Expense WHERE id =?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
