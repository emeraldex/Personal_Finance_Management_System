package com.expense.desktop.ui;

import com.expense.core.report.CategoryBreakdownItem;
import com.expense.desktop.viewmodel.DashboardViewModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.Objects;

/**
 * Pure View: builds the dashboard scene graph and binds every label to the
 * {@link DashboardViewModel}. Contains no business logic. The category pie chart
 * refreshes whenever the ViewModel's breakdown list changes. A month pager lets
 * the user page through history, and export buttons save the current month as CSV.
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

        Button prev = new Button("◀ Prev");
        prev.setOnAction(e -> vm.prevMonth());
        Button next = new Button("Next ▶");
        next.setOnAction(e -> vm.nextMonth());
        Button today = new Button("This month");
        today.setOnAction(e -> vm.loadCurrentMonth());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button exportSummary = new Button("Export summary CSV");
        exportSummary.setOnAction(e -> saveCsv(e.getSource(), "expense-summary-" + vm.currentMonthStem(),
                vm::exportSummaryCsv));
        Button exportTx = new Button("Export transactions CSV");
        exportTx.setOnAction(e -> saveCsv(e.getSource(), "expense-transactions-" + vm.currentMonthStem(),
                vm::exportTransactionsCsv));
        HBox toolbar = new HBox(8, prev, today, next, spacer, exportSummary, exportTx);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        HBox cards = new HBox(12,
                card("Income", vm, "income"),
                card("Expenses", vm, "expense"),
                card("Net", vm, "net"),
                card("Savings", vm, "savings"),
                card("Outstanding", vm, "outstanding"));

        PieChart pie = new PieChart();
        pie.setTitle("Spending by category");
        vm.getCategoryBreakdown().addListener((javafx.collections.ListChangeListener<CategoryBreakdownItem>) c -> {
            pie.getData().clear();
            for (CategoryBreakdownItem item : vm.getCategoryBreakdown()) {
                pie.getData().add(new PieChart.Data(
                        item.categoryName(), item.total().abs().amount().doubleValue()));
            }
        });

        Label status = new Label();
        status.textProperty().bind(vm.statusProperty());
        status.setStyle("-fx-text-fill: #444;");

        VBox root = new VBox(16, title, toolbar, cards, pie, status);
        root.setPadding(new Insets(16));
        VBox.setVgrow(pie, Priority.ALWAYS);
        return root;
    }

    private void saveCsv(Object source, String suggestedName, java.util.function.Function<File, Boolean> exporter) {
        Window window = ((Node) source).getScene().getWindow();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export CSV");
        chooser.setInitialFileName(suggestedName + ".csv");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
        File file = chooser.showSaveDialog(window);
        if (file != null) {
            exporter.apply(file);
        }
    }

    private VBox card(String label, DashboardViewModel vm, String key) {
        Label caption = new Label(label);
        caption.setStyle("-fx-text-fill: #666;");
        Label value = new Label();
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
