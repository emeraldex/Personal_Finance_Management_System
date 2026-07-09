package com.expense.desktop;

import com.expense.core.service.ExpenseManager;

import java.util.Currency;

/**
 * Process-wide holder for the {@link ExpenseManager} composition root. A tiny
 * service locator is sufficient here; ViewModels receive the manager by
 * constructor injection so they remain independently testable.
 */
public final class AppContext {

    private static ExpenseManager manager;

    private AppContext() {
    }

    public static void initFile(String dbPath, Currency currency) {
        manager = ExpenseManager.openFile(dbPath, currency);
    }

    public static ExpenseManager manager() {
        if (manager == null) {
            throw new IllegalStateException("AppContext not initialised");
        }
        return manager;
    }

    public static void shutdown() {
        if (manager != null) {
            manager.close();
            manager = null;
        }
    }
}
