package com.expense.desktop.ui;

import com.expense.core.report.CategoryBreakdownItem;
import com.expense.desktop.viewmodel.DashboardViewModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Objects;

/**
 * Pure View: builds the dashboard scene graph and binds every label to the
 * {@link DashboardViewModel}. Contains no business logic. The category pie chart
 * refreshes whenever the ViewModel's breakdown list changes.
 */
public final class DashboardView {

    private final DashboardViewModel vm;

    public DashboardView(DashboardViewModel vm) {
        this.vm = Objects.requireNonNull(vm);
    }

    public VBox build() {
        Label title = new Label();
        title.textProperty().bind(vm.monthProperty().concat(" overview"));
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        HBox cards = new HBox(12,
                card("Income", vm.totalIncomeProperty().get(), vm, "income"),
                card("Expenses", vm.totalExpenseProperty().get(), vm, "expense"),
                card("Net", vm.netBalanceProperty().get(), vm, "net"),
                card("Savings", vm.savingsProperty().get(), vm, "savings"),
                card("Outstanding", vm.outstandingProperty().get(), vm, "outstanding"));

        PieChart pie = new PieChart();
        pie.setTitle("Spending by category");
        vm.getCategoryBreakdown().addListener((javafx.collections.ListChangeListener<CategoryBreakdownItem>) c -> {
            pie.getData().clear();
            for (CategoryBreakdownItem item : vm.getCategoryBreakdown()) {
                pie.getData().add(new PieChart.Data(
                        item.categoryName(), item.total().abs().amount().doubleValue()));
            }
        });

        VBox root = new VBox(16, title, cards, pie);
        root.setPadding(new Insets(16));
        VBox.setVgrow(pie, Priority.ALWAYS);
        return root;
    }

    private VBox card(String label, String initial, DashboardViewModel vm, String key) {
        Label caption = new Label(label);
        caption.setStyle("-fx-text-fill: #666;");
        Label value = new Label(initial);
        value.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        switch (key) {
            case "income" -> value.textProperty().bind(vm.totalIncomeProperty());
            case "expense" -> value.textProperty().bind(vm.totalExpenseProperty());
            case "net" -> value.textProperty().bind(vm.netBalanceProperty());
            case "savings" -> value.textProperty().bind(vm.savingsProperty());
            case "outstanding" -> value.textProperty().bind(vm.outstandingProperty());
            default -> { }
        }
        VBox box = new VBox(4, caption, value);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: #f4f6f8; -fx-background-radius: 8;");
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }
}
