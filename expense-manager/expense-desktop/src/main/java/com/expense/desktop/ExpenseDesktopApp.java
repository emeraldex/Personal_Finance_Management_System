package com.expense.desktop;

import com.expense.desktop.ui.BudgetView;
import com.expense.desktop.ui.DashboardView;
import com.expense.desktop.ui.ExpenseView;
import com.expense.desktop.ui.HistoryView;
import com.expense.desktop.ui.IncomeView;
import com.expense.desktop.ui.ManageView;
import com.expense.desktop.viewmodel.BudgetViewModel;
import com.expense.desktop.viewmodel.DashboardViewModel;
import com.expense.desktop.viewmodel.ExpenseFormViewModel;
import com.expense.desktop.viewmodel.HistoryViewModel;
import com.expense.desktop.viewmodel.IncomeFormViewModel;
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
        var manager = AppContext.manager();
        // A holder breaks the construction cycle: the read VMs need the shared
        // refresh, and the refresh needs the read VMs. It is assigned before any
        // user action can fire it.
        final Runnable[] refreshHolder = new Runnable[1];
        Runnable refreshAll = () -> refreshHolder[0].run();

        DashboardViewModel dashboardVm = new DashboardViewModel(manager);
        HistoryViewModel historyVm = new HistoryViewModel(manager, refreshAll);
        BudgetViewModel budgetVm = new BudgetViewModel(manager, refreshAll);

        // Any transaction change refreshes the read screens (dashboard, history, budgets).
        refreshHolder[0] = () -> {
            dashboardVm.reload();
            historyVm.reload();
            budgetVm.reload();
        };

        ExpenseFormViewModel expenseVm = new ExpenseFormViewModel(manager, refreshAll);
        IncomeFormViewModel incomeVm = new IncomeFormViewModel(manager, refreshAll);
        // When master data changes on the Manage tab, refresh the entry-form pickers.
        ManageViewModel manageVm = new ManageViewModel(manager, CURRENCY, () -> {
            expenseVm.refreshLookups();
            incomeVm.refreshLookups();
        });

        TabPane tabs = new TabPane();
        tabs.getTabs().add(new Tab("Dashboard", new DashboardView(dashboardVm).build()));
        tabs.getTabs().add(new Tab("Add Expense", new ExpenseView(expenseVm).build()));
        tabs.getTabs().add(new Tab("Add Income", new IncomeView(incomeVm).build()));
        tabs.getTabs().add(new Tab("History", new HistoryView(historyVm).build()));
        tabs.getTabs().add(new Tab("Budgets", new BudgetView(budgetVm).build()));
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
