package com.example.expensedesktop.controller;

import com.example.expensecore.FirebaseAuthService;
import com.example.expensecore.FirebaseDatabaseService;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private TextField passwordField;

    private final FirebaseAuthService firebaseAuthService =
        new FirebaseAuthService();
    private final FirebaseDatabaseService firebaseDatabaseService =
        new FirebaseDatabaseService();

    @FXML
    private void login() {
        String email = emailField.getText();
        String password = passwordField.getText();
        firebaseAuthService.signIn(email, password);
        firebaseDatabaseService.syncDataOnUserLogin();
    }

    @FXML
    private void logout() {
        firebaseAuthService.signOut();
    }
}
