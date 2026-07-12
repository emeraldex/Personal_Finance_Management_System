package com.expense.desktop.ui;

import com.expense.core.domain.Account;
import com.expense.core.domain.Category;
import com.expense.core.domain.Expense;
import com.expense.core.domain.PaymentMethod;
import com.expense.core.domain.Transaction;
import com.expense.desktop.viewmodel.HistoryViewModel;
import com.expense.desktop.viewmodel.HistoryViewModel.Row;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.Objects;
import java.util.function.Function;

/**
 * Pure View for the transaction-history tab. A month pager and table list the
 * month's transactions; selecting a row opens an inline edit panel wired to the
 * {@link HistoryViewModel}. The category picker and payment field adapt to whether
 * the selected row is an expense or income.
 */
public final class HistoryView {

    private final HistoryViewModel vm;

    public HistoryView(HistoryViewModel vm) {
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

        TableView<Row> table = new TableView<>(vm.getRows());
        table.getColumns().add(column("Date", Row::date, 100));
        table.getColumns().add(column("Type", Row::type, 80));
        table.getColumns().add(column("Amount", Row::amount, 130));
        table.getColumns().add(column("Category", Row::category, 130));
        table.getColumns().add(column("Account", Row::account, 120));
        table.getColumns().add(column("Payment", Row::paymentMethod, 110));
        table.getColumns().add(column("Description", Row::description, 200));

        // --- inline edit panel -------------------------------------------
        ComboBox<Account> accountBox = new ComboBox<>(vm.getAccounts());
        accountBox.setConverter(converter(a -> a == null ? "" : a.name()));
        ComboBox<Category> categoryBox = new ComboBox<>();
        categoryBox.setConverter(converter(c -> c == null ? "" : c.name()));
        ComboBox<PaymentMethod> paymentBox = new ComboBox<>(vm.getPaymentMethods());
        paymentBox.setConverter(converter(p -> p == null ? "" : p.name()));
        TextField amount = new TextField();
        amount.setPromptText("amount");
        TextField description = new TextField();
        description.setPromptText("description");
        DatePicker date = new DatePicker();

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);
        form.addRow(0, new Label("Account"), accountBox, new Label("Category"), categoryBox);
        form.addRow(1, new Label("Payment"), paymentBox, new Label("Date"), date);
        form.addRow(2, new Label("Amount"), amount, new Label("Description"), description);

        Button save = new Button("Save changes");
        Button delete = new Button("Delete");
        Label status = new Label();
        status.textProperty().bind(vm.statusProperty());
        HBox actions = new HBox(8, save, delete, status);
        actions.setAlignment(Pos.CENTER_LEFT);

        Label editHint = new Label("Select a transaction above to edit or delete it.");
        editHint.setStyle("-fx-text-fill: #666;");
        VBox editPanel = new VBox(8, editHint, form, actions);
        editPanel.setPadding(new Insets(12, 0, 0, 0));

        // Populate the edit panel when a row is selected.
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, row) -> {
            if (row == null) {
                return;
            }
            Transaction tx = row.tx();
            boolean isExpense = tx instanceof Expense;
            categoryBox.setItems(isExpense ? vm.getExpenseCategories() : vm.getIncomeCategories());
            paymentBox.setDisable(!isExpense);
            selectById(accountBox, tx.accountId());
            selectCategory(categoryBox, tx.categoryId());
            if (isExpense) {
                Long pmId = ((Expense) tx).paymentMethodId();
                selectPayment(paymentBox, pmId);
            } else {
                paymentBox.setValue(null);
            }
            amount.setText(tx.signedAmount().abs().amount().toPlainString());
            description.setText(tx.description());
            date.setValue(tx.date());
        });

        save.setOnAction(e -> {
            Row selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                vm.saveEdit(selected.tx(), accountBox.getValue(), categoryBox.getValue(),
                        paymentBox.getValue(), amount.getText(), description.getText(), date.getValue());
            } else {
                // status is driven by the VM; nudge the user via a transient selection prompt.
                vm.saveEdit(null, null, null, null, "", "", LocalDate.now());
            }
        });
        delete.setOnAction(e -> vm.delete(
                table.getSelectionModel().getSelectedItem() == null
                        ? null : table.getSelectionModel().getSelectedItem().tx()));

        VBox root = new VBox(12, pager, table, editPanel);
        root.setPadding(new Insets(16));
        VBox.setVgrow(table, Priority.ALWAYS);
        return root;
    }

    private void selectById(ComboBox<Account> box, long accountId) {
        for (Account a : box.getItems()) {
            if (a.id() != null && a.id() == accountId) {
                box.setValue(a);
                return;
            }
        }
        box.setValue(null);
    }

    private void selectCategory(ComboBox<Category> box, Long categoryId) {
        if (categoryId == null) {
            box.setValue(null);
            return;
        }
        for (Category c : box.getItems()) {
            if (categoryId.equals(c.id())) {
                box.setValue(c);
                return;
            }
        }
        box.setValue(null);
    }

    private void selectPayment(ComboBox<PaymentMethod> box, Long paymentId) {
        if (paymentId == null) {
            box.setValue(null);
            return;
        }
        for (PaymentMethod p : box.getItems()) {
            if (paymentId.equals(p.id())) {
                box.setValue(p);
                return;
            }
        }
        box.setValue(null);
    }

    private TableColumn<Row, String> column(String title, Function<Row, String> value, double width) {
        TableColumn<Row, String> col = new TableColumn<>(title);
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
