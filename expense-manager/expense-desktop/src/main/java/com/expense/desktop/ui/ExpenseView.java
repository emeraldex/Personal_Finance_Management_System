package com.expense.desktop.ui;

import com.expense.core.domain.Account;
import com.expense.core.domain.Category;
import com.expense.core.domain.PaymentMethod;
import com.expense.desktop.viewmodel.ExpenseFormViewModel;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.util.Objects;

/**
 * Pure View for adding an expense. Binds inputs to {@link ExpenseFormViewModel}
 * and, on a successful save, asks the {@link DashboardViewModel} to refresh so
 * the dashboard reflects the new entry immediately.
 */
public final class ExpenseView {

    private final ExpenseFormViewModel vm;

    public ExpenseView(ExpenseFormViewModel vm) {
        this.vm = Objects.requireNonNull(vm);
    }

    public GridPane build() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        ComboBox<Account> accountBox = new ComboBox<>(vm.getAccounts());
        accountBox.valueProperty().bindBidirectional(vm.accountProperty());
        accountBox.setConverter(converter(a -> a == null ? "" : a.name()));

        ComboBox<Category> categoryBox = new ComboBox<>(vm.getCategories());
        categoryBox.valueProperty().bindBidirectional(vm.categoryProperty());
        categoryBox.setConverter(converter(c -> c == null ? "" : c.name()));
        Button suggest = new Button("Suggest");
        suggest.setOnAction(e -> vm.suggestCategory());
        HBox categoryRow = new HBox(8, categoryBox, suggest);

        ComboBox<PaymentMethod> paymentBox = new ComboBox<>(vm.getPaymentMethods());
        paymentBox.valueProperty().bindBidirectional(vm.paymentMethodProperty());
        paymentBox.setConverter(converter(p -> p == null ? "" : p.name()));

        TextField amount = new TextField();
        amount.textProperty().bindBidirectional(vm.amountProperty());
        amount.setPromptText("e.g. 42.50");

        TextField description = new TextField();
        description.textProperty().bindBidirectional(vm.descriptionProperty());

        DatePicker date = new DatePicker();
        date.valueProperty().bindBidirectional(vm.dateProperty());

        Label status = new Label();
        status.textProperty().bind(vm.statusProperty());

        Button save = new Button("Save expense");
        save.setOnAction(e -> vm.save());

        grid.addRow(0, new Label("Account"), accountBox);
        grid.addRow(1, new Label("Category"), categoryRow);
        grid.addRow(2, new Label("Payment"), paymentBox);
        grid.addRow(3, new Label("Amount"), amount);
        grid.addRow(4, new Label("Description"), description);
        grid.addRow(5, new Label("Date"), date);
        grid.addRow(6, save, status);
        return grid;
    }

    private <T> StringConverter<T> converter(java.util.function.Function<T, String> toString) {
        return new StringConverter<>() {
            @Override public String toString(T object) { return toString.apply(object); }
            @Override public T fromString(String string) { return null; }
        };
    }
}
