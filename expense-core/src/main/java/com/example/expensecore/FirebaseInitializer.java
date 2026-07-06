package com.example.expensecore;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class FirebaseInitializer {
    public static void initialize() {
        FirebaseApp.initializeApp(new FirebaseOptions.Builder()
          .setApiKey("YOUR_API_KEY")
          .setApplicationId("YOUR_APPLICATION_ID")
          .setDatabaseUrl("YOUR_DATABASE_URL")
          .build());
    }
}
