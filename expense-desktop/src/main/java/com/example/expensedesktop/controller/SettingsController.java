package com.example.expensedesktop.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class SettingsController {

    @FXML
    private TextField databasePathField;

    @FXML
    private void saveSettings() {
        String databasePath = databasePathField.getText();
        // Implement saving settings logic
    }
}
