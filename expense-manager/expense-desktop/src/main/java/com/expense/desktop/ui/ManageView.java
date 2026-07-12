package com.expense.desktop.ui;

import com.expense.core.domain.Account;
import com.expense.core.domain.AccountType;
import com.expense.core.domain.Category;
import com.expense.core.domain.CategoryType;
import com.expense.core.domain.PaymentMethod;
import com.expense.core.domain.PaymentMethodType;
import com.expense.desktop.viewmodel.ManageViewModel;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Objects;

/**
 * Pure View for the "Manage" tab. Presents accounts, categories and payment
 * methods each in their own sub-tab with a table, a delete button and a small
 * create form, all bound to the {@link ManageViewModel}. Contains no business
 * logic — every mutation is delegated to the ViewModel, which reports the
 * outcome through a shared status label.
 */
public final class ManageView {

    private final ManageViewModel vm;

    public ManageView(ManageViewModel vm) {
        this.vm = Objects.requireNonNull(vm);
    }

    public VBox build() {
        TabPane inner = new TabPane();
        inner.getTabs().add(nonClosable("Accounts", accountsPane()));
        inner.getTabs().add(nonClosable("Categories", categoriesPane()));
        inner.getTabs().add(nonClosable("Payment Methods", paymentMethodsPane()));

        Label status = new Label();
        status.textProperty().bind(vm.statusProperty());
        status.setStyle("-fx-text-fill: #444;");

        VBox root = new VBox(10, inner, status);
        root.setPadding(new Insets(16));
        VBox.setVgrow(inner, Priority.ALWAYS);
        return root;
    }

    // ---- Accounts -------------------------------------------------------

    private VBox accountsPane() {
        TableView<Account> table = new TableView<>(vm.getAccounts());
        table.getColumns().add(column("Name", Account::name));
        table.getColumns().add(column("Type", a -> a.type().name()));
        table.getColumns().add(column("Opening Balance", a -> a.openingBalance().toString()));
        table.getColumns().add(column("Archived", a -> a.archived() ? "yes" : ""));

        TextField name = new TextField();
        name.setPromptText("Account name");
        ComboBox<AccountType> type = new ComboBox<>(FXCollections.observableArrayList(AccountType.values()));
        type.setValue(AccountType.CASH);
        TextField opening = new TextField();
        opening.setPromptText("Opening balance (e.g. 0)");
        opening.setText("0");

        Button add = new Button("Add");
        add.setOnAction(e -> {
            if (vm.createAccount(name.getText(), type.getValue(), opening.getText())) {
                name.clear();
                opening.setText("0");
            }
        });
        Button delete = deleteButton(() -> vm.deleteAccount(table.getSelectionModel().getSelectedItem()));

        HBox form = new HBox(8, name, type, opening, add);
        return section(table, delete, form);
    }

    // ---- Categories -----------------------------------------------------

    private VBox categoriesPane() {
        TableView<Category> table = new TableView<>(vm.getCategories());
        table.getColumns().add(column("Name", Category::name));
        table.getColumns().add(column("Type", c -> c.type().name()));
        table.getColumns().add(column("Archived", c -> c.archived() ? "yes" : ""));

        TextField name = new TextField();
        name.setPromptText("Category name");
        ComboBox<CategoryType> type = new ComboBox<>(FXCollections.observableArrayList(CategoryType.values()));
        type.setValue(CategoryType.EXPENSE);

        Button add = new Button("Add");
        add.setOnAction(e -> {
            if (vm.createCategory(name.getText(), type.getValue())) {
                name.clear();
            }
        });
        Button delete = deleteButton(() -> vm.deleteCategory(table.getSelectionModel().getSelectedItem()));

        HBox form = new HBox(8, name, type, add);
        return section(table, delete, form);
    }

    // ---- Payment methods ------------------------------------------------

    private VBox paymentMethodsPane() {
        TableView<PaymentMethod> table = new TableView<>(vm.getPaymentMethods());
        table.getColumns().add(column("Name", PaymentMethod::name));
        table.getColumns().add(column("Type", p -> p.type().name()));
        table.getColumns().add(column("Archived", p -> p.archived() ? "yes" : ""));

        TextField name = new TextField();
        name.setPromptText("Payment method name");
        ComboBox<PaymentMethodType> type =
                new ComboBox<>(FXCollections.observableArrayList(PaymentMethodType.values()));
        type.setValue(PaymentMethodType.CASH);

        Button add = new Button("Add");
        add.setOnAction(e -> {
            if (vm.createPaymentMethod(name.getText(), type.getValue())) {
                name.clear();
            }
        });
        Button delete = deleteButton(() -> vm.deletePaymentMethod(table.getSelectionModel().getSelectedItem()));

        HBox form = new HBox(8, name, type, add);
        return section(table, delete, form);
    }

    // ---- shared building blocks ----------------------------------------

    private VBox section(TableView<?> table, Button delete, HBox form) {
        HBox actions = new HBox(8, delete);
        VBox box = new VBox(10, table, actions, new Label("Add new:"), form);
        box.setPadding(new Insets(12));
        VBox.setVgrow(table, Priority.ALWAYS);
        return box;
    }

    private Button deleteButton(Runnable action) {
        Button delete = new Button("Delete selected");
        delete.setOnAction(e -> action.run());
        return delete;
    }

    private <S> TableColumn<S, String> column(String title, java.util.function.Function<S, String> value) {
        TableColumn<S, String> col = new TableColumn<>(title);
        col.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(value.apply(cell.getValue())));
        col.setPrefWidth(160);
        return col;
    }

    private Tab nonClosable(String title, javafx.scene.Node content) {
        Tab tab = new Tab(title, content);
        tab.setClosable(false);
        return tab;
    }
}
