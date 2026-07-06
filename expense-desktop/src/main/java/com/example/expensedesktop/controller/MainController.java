package com.example.expensedesktop.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    @FXML
    private void showExpenseScreen() {
        loadScreen("ExpenseScreen.fxml");
    }

    @FXML
    private void showIncomeScreen() {
        loadScreen("IncomeScreen.fxml");
    }

    @FXML
    private void showDashboardScreen() {
        loadScreen("DashboardScreen.fxml");
    }

    @FXML
    private void showReportsScreen() {
        loadScreen("ReportsScreen.fxml");
    }

    @FXML
    private void showSettingsScreen() {
        loadScreen("SettingsScreen.fxml");
    }

    private void loadScreen(String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("../fxml/" + fxmlFile));
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
