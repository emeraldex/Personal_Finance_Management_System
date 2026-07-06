package com.example.expensedesktop.controller;

import com.example.expensecore.FirebaseAuthService;
import com.example.expensecore.FirebaseDatabaseService;
import com.example.expensecore.UserService;
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
    private final UserService userService = new UserService(
        new ExpenseService(
            new com.example.expensecore.repository.JdbcExpenseRepository(null)
        ),
        new IncomeService(
            new com.example.expensecore.repository.JdbcIncomeRepository(null)
        )
    );

    @FXML
    private void login() {
        String email = emailField.getText();
        String password = passwordField.getText();
        firebaseAuthService.signIn(email, password);
        userService.setCurrentUser(firebaseAuthService.getCurrentUser());
        // Sync data with Firebase
    }

    @FXML
    private void logout() {
        firebaseAuthService.signOut();
        userService.setCurrentUser(null);
    }
}
