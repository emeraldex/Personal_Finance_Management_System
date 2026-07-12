package com.expense.desktop.ui;

import com.expense.desktop.viewmodel.SettingsViewModel;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.Objects;

/**
 * Pure View for the Settings / Data tab: an auto-categorise toggle, a database
 * backup action, an Excel import action, and read-only environment details.
 */
public final class SettingsView {

    private final SettingsViewModel vm;

    public SettingsView(SettingsViewModel vm) {
        this.vm = Objects.requireNonNull(vm);
    }

    public VBox build() {
        Label heading = new Label("Settings");
        heading.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        CheckBox autoCategorize = new CheckBox(
                "Automatically suggest a category from the description when saving an expense");
        autoCategorize.selectedProperty().bindBidirectional(vm.autoCategorizeProperty());

        Button backup = new Button("Back up database…");
        backup.setOnAction(e -> {
            Window window = ((Node) e.getSource()).getScene().getWindow();
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Back up database");
            chooser.setInitialFileName("expenses-backup.db");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQLite database", "*.db"));
            File file = chooser.showSaveDialog(window);
            if (file != null) {
                vm.backupDatabase(file);
            }
        });

        Button importExcel = new Button("Import transactions from Excel…");
        importExcel.setOnAction(e -> {
            Window window = ((Node) e.getSource()).getScene().getWindow();
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Import Excel workbook");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel workbook", "*.xlsx"));
            File file = chooser.showOpenDialog(window);
            if (file != null) {
                vm.importExcel(file);
            }
        });
        Label importHint = new Label(
                "Expected columns: Date, Type, Amount, Category, Account, PaymentMethod, Description. "
                        + "Missing accounts/categories/payment methods are created automatically.");
        importHint.setStyle("-fx-text-fill: #666;");
        importHint.setWrapText(true);

        Label info = new Label("Data file: " + vm.getDatabasePath()
                + "\nCurrency: " + vm.getCurrencyCode());
        info.setStyle("-fx-text-fill: #666;");

        Label status = new Label();
        status.textProperty().bind(vm.statusProperty());
        status.setWrapText(true);

        VBox root = new VBox(14,
                heading,
                new Label("Entry"), autoCategorize,
                new Separator(),
                new Label("Data"), new HBox(8, backup, importExcel), importHint,
                new Separator(),
                info,
                status);
        root.setPadding(new Insets(16));
        return root;
    }
}
