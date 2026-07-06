package com.example.expensecore.repository;

import com.example.expensecore.domain.Income;
import com.example.expensecore.dto.IncomeDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcIncomeRepository implements IncomeRepository {

    private final Connection connection;

    public JdbcIncomeRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<Income> getAllIncomes() {
        List<Income> incomes = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM Income")) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Income income = new Income(
                    resultSet.getInt("id"),
                    resultSet.getString("date"),
                    resultSet.getString("description"),
                    resultSet.getDouble("amount"),
                    resultSet.getInt("category_id"),
                    resultSet.getInt("payment_method_id"),
                    resultSet.getInt("account_id")
                );
                incomes.add(income);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return incomes;
    }

    @Override
    public Income getIncomeById(int id) {
        Income income = null;
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM Income WHERE id =?")) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                income = new Income(
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
        return income;
    }

    @Override
    public void addIncome(IncomeDTO incomeDTO) {
        try (PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO Income (date, description, amount, category_id, payment_method_id, account_id) VALUES (?,?,?,?,?,?)")) {
            statement.setString(1, incomeDTO.getDate());
            statement.setString(2, incomeDTO.getDescription());
            statement.setDouble(3, incomeDTO.getAmount());
            statement.setInt(4, incomeDTO.getCategoryId());
            statement.setInt(5, incomeDTO.getPaymentMethodId());
            statement.setInt(6, incomeDTO.getAccountId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateIncome(IncomeDTO incomeDTO) {
        try (PreparedStatement statement = connection.prepareStatement(
            "UPDATE Income SET date =?, description =?, amount =?, category_id =?, payment_method_id =?, account_id =? WHERE id =?")) {
            statement.setString(1, incomeDTO.getDate());
            statement.setString(2, incomeDTO.getDescription());
            statement.setDouble(3, incomeDTO.getAmount());
            statement.setInt(4, incomeDTO.getCategoryId());
            statement.setInt(5, incomeDTO.getPaymentMethodId());
            statement.setInt(6, incomeDTO.getAccountId());
            statement.setInt(7, incomeDTO.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteIncome(int id) {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM Income WHERE id =?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
