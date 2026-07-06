package com.example.expensedesktop.controller;

import java.io.FileWriter;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class SettingsController {

    @FXML
    private TextField databasePathField;

    @FXML
    private void saveSettings() {
        String databasePath = databasePathField.getText();
        try (FileWriter writer = new FileWriter("config.properties")) {
            writer.write("database.path=" + databasePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
