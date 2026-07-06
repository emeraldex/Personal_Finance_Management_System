package com.example.expensecore;

import com.example.expensecore.dto.ExpenseDTO;
import com.example.expensecore.dto.IncomeDTO;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class FirebaseDatabaseService {

    private final DatabaseReference databaseReference;

    public FirebaseDatabaseService() {
        FirebaseInitializer.initialize();
        this.databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public void syncExpenses(List<ExpenseDTO> expenses) {
        databaseReference.child("expenses").setValue(expenses);
        databaseReference.child("expenses").addValueEventListener(
            new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<ExpenseDTO> firebaseExpenses = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        ExpenseDTO expense = snapshot.getValue(
                            ExpenseDTO.class
                        );
                        firebaseExpenses.add(expense);
                    }
                    // Update local SQLite database with firebaseExpenses
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle possible errors
                }
            }
        );
    }

    public void syncIncomes(List<IncomeDTO> incomes) {
        databaseReference.child("incomes").setValue(incomes);
        databaseReference.child("incomes").addValueEventListener(
            new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<IncomeDTO> firebaseIncomes = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        IncomeDTO income = snapshot.getValue(IncomeDTO.class);
                        firebaseIncomes.add(income);
                    }
                    // Update local SQLite database with firebaseIncomes
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle possible errors
                }
            }
        );
    }
}
