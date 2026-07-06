package com.example.expenseandroid.controller;

import com.example.expensecore.FirebaseAuthService;
import com.example.expensecore.FirebaseDatabaseService;
import com.example.expensecore.UserService;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuthService firebaseAuthService = new FirebaseAuthService();
    private FirebaseDatabaseService firebaseDatabaseService = new FirebaseDatabaseService();
    private UserService userService = new UserService(new ExpenseService(new com.example.expensecore.repository.JdbcExpenseRepository(null)), new IncomeService(new com.example.expensecore.repository.JdbcIncomeRepository(null)));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText emailField = findViewById(R.id.emailField);
        EditText passwordField = findViewById(R.id.passwordField);
        Button loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(v -> {
            String email = emailField.getText().toString();
            String password = passwordField.getText().toString();
            firebaseAuthService.signIn(email, password);
            userService.setCurrentUser(firebaseAuthService.getCurrentUser());
            // Sync data with Firebase
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        firebaseAuthService.signOut();
        userService.setCurrentUser(null);
    }
}
