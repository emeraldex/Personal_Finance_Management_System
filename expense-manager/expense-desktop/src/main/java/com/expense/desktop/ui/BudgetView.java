package com.expense.desktop.ui;

import com.expense.core.domain.Category;
import com.expense.core.report.BudgetUtilization;
import com.expense.desktop.viewmodel.BudgetViewModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.Objects;
import java.util.function.Function;

/**
 * Pure View for the budgets tab. A month pager and table show budget-versus-actual
 * for each capped category; a small form sets a new cap and a button removes the
 * selected one. All logic lives in {@link BudgetViewModel}.
 */
public final class BudgetView {

    private final BudgetViewModel vm;

    public BudgetView(BudgetViewModel vm) {
        this.vm = Objects.requireNonNull(vm);
    }

    public VBox build() {
        Label monthLabel = new Label();
        monthLabel.textProperty().bind(vm.monthProperty());
        monthLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        Button prev = new Button("◀ Prev");
        prev.setOnAction(e -> vm.prevMonth());
        Button next = new Button("Next ▶");
        next.setOnAction(e -> vm.nextMonth());
        HBox pager = new HBox(8, prev, monthLabel, next);
        pager.setAlignment(Pos.CENTER_LEFT);

        TableView<BudgetUtilization> table = new TableView<>(vm.getUtilizations());
        table.getColumns().add(column("Category", BudgetUtilization::categoryName, 150));
        table.getColumns().add(column("Limit", b -> b.limit().toString(), 120));
        table.getColumns().add(column("Spent", b -> b.spent().toString(), 120));
        table.getColumns().add(column("Remaining", b -> b.remaining().toString(), 120));
        table.getColumns().add(column("Used %", b -> String.valueOf(b.utilizationPct()), 90));
        table.getColumns().add(column("Over?", b -> b.overBudget() ? "OVER" : "", 70));

        ComboBox<Category> categoryBox = new ComboBox<>(vm.getExpenseCategories());
        categoryBox.setConverter(converter(c -> c == null ? "" : c.name()));
        categoryBox.setPromptText("Category");
        TextField limit = new TextField();
        limit.setPromptText("Monthly cap, e.g. 500");
        Button set = new Button("Set budget");
        set.setOnAction(e -> vm.setBudget(categoryBox.getValue(), limit.getText()));
        Button delete = new Button("Delete selected");
        delete.setOnAction(e -> vm.deleteSelected(table.getSelectionModel().getSelectedItem()));

        HBox form = new HBox(8, categoryBox, limit, set, delete);
        form.setAlignment(Pos.CENTER_LEFT);

        Label status = new Label();
        status.textProperty().bind(vm.statusProperty());
        status.setStyle("-fx-text-fill: #444;");

        VBox root = new VBox(12, pager, table, new Label("Set or remove a cap:"), form, status);
        root.setPadding(new Insets(16));
        VBox.setVgrow(table, Priority.ALWAYS);
        return root;
    }

    private TableColumn<BudgetUtilization, String> column(String title,
            Function<BudgetUtilization, String> value, double width) {
        TableColumn<BudgetUtilization, String> col = new TableColumn<>(title);
        col.setCellValueFactory(cell -> new SimpleStringProperty(value.apply(cell.getValue())));
        col.setPrefWidth(width);
        return col;
    }

    private <T> StringConverter<T> converter(Function<T, String> toString) {
        return new StringConverter<>() {
            @Override public String toString(T object) { return toString.apply(object); }
            @Override public T fromString(String string) { return null; }
        };
    }
}
