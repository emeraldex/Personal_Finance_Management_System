package com.expense.desktop.ui;

import com.expense.core.report.CategoryBreakdownItem;
import com.expense.desktop.viewmodel.DashboardViewModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.Objects;
import java.util.function.Function;

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
        HBox pager = new HBox(8, prev, today, next);
        pager.setAlignment(Pos.CENTER_LEFT);

        FlowPane exports = new FlowPane(8, 8,
                exportButton("Summary CSV", "expense-summary-", "csv", vm::exportSummaryCsv),
                exportButton("Summary XLSX", "expense-summary-", "xlsx", vm::exportSummaryXlsx),
                exportButton("Summary PDF", "expense-summary-", "pdf", vm::exportSummaryPdf),
                exportButton("Transactions CSV", "expense-transactions-", "csv", vm::exportTransactionsCsv),
                exportButton("Transactions XLSX", "expense-transactions-", "xlsx", vm::exportTransactionsXlsx));

        VBox toolbar = new VBox(8, pager, new Label("Export current month:"), exports);

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

    private Button exportButton(String label, String stemPrefix, String ext,
                                Function<File, Boolean> exporter) {
        Button button = new Button(label);
        button.setOnAction(e -> {
            Window window = ((Node) e.getSource()).getScene().getWindow();
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Export " + label);
            chooser.setInitialFileName(stemPrefix + vm.currentMonthStem() + "." + ext);
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(ext.toUpperCase() + " files", "*." + ext));
            File file = chooser.showSaveDialog(window);
            if (file != null) {
                exporter.apply(file);
            }
        });
        return button;
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
