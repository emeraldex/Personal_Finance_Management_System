package com.expense.desktop;

/**
 * Packaging entry point. Delegating to the JavaFX app from a non-Application
 * main class lets the shaded jar start without the JavaFX module path.
 */
public final class Launcher {
    public static void main(String[] args) {
        ExpenseDesktopApp.main(args);
    }
}
