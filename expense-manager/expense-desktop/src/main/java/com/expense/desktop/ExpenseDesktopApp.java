package com.expense.desktop;

import com.expense.desktop.ui.DashboardView;
import com.expense.desktop.ui.ExpenseView;
import com.expense.desktop.ui.ManageView;
import com.expense.desktop.viewmodel.DashboardViewModel;
import com.expense.desktop.viewmodel.ExpenseFormViewModel;
import com.expense.desktop.viewmodel.ManageViewModel;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.Currency;

/**
 * JavaFX entry point. Wires the core once via {@link AppContext} then builds the
 * shell with a Dashboard tab and an Expense-entry tab. Each tab hosts a View
 * bound to its own ViewModel (MVVM); Views never touch persistence directly.
 */
public final class ExpenseDesktopApp extends Application {

    private static final Currency CURRENCY = Currency.getInstance("MYR");

    @Override
    public void init() {
        String dbPath = Path.of(System.getProperty("user.home"), ".expense-manager", "expenses.db")
                .toString();
        AppContext.initFile(dbPath, CURRENCY);
        // Give a fresh database usable starter data so the pickers are never empty.
        DefaultData.seedIfEmpty(AppContext.manager(), CURRENCY);
    }

    @Override
    public void start(Stage stage) {
        DashboardViewModel dashboardVm = new DashboardViewModel(AppContext.manager());
        ExpenseFormViewModel expenseVm = new ExpenseFormViewModel(AppContext.manager());
        // When master data changes on the Manage tab, refresh the Add-Expense pickers.
        ManageViewModel manageVm = new ManageViewModel(
                AppContext.manager(), CURRENCY, expenseVm::refreshLookups);

        TabPane tabs = new TabPane();
        tabs.getTabs().add(new Tab("Dashboard", new DashboardView(dashboardVm).build()));
        tabs.getTabs().add(new Tab("Add Expense", new ExpenseView(expenseVm, dashboardVm).build()));
        tabs.getTabs().add(new Tab("Manage", new ManageView(manageVm).build()));
        tabs.getTabs().forEach(t -> t.setClosable(false));

        BorderPane root = new BorderPane(tabs);
        root.setPadding(new Insets(12));

        dashboardVm.loadCurrentMonth();

        stage.setTitle("Personal Finance & Expense Manager");
        stage.setScene(new Scene(root, 900, 620));
        stage.show();
    }

    @Override
    public void stop() {
        AppContext.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
