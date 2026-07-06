package com.example.expensecore.service;

import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import com.example.expensecore.dto.ExpenseDTO;

public class ExpenseCategorizer {
    private final Graph graph;
    private final Session session;

    public ExpenseCategorizer() {
        this.graph = new Graph();
        try (Tensor<String> model = Tensor.create(getClass().getResourceAsStream("model.pb"))) {
            graph.importGraphDef(model);
        }
        this.session = new Session(graph);
    }

    public String categorizeExpense(ExpenseDTO expense) {
        // Implement logic to use the TensorFlow model to categorize the expense
        // Example: Use the description and amount to predict the category
        // return predictedCategory;
    }
}
