# expense-android

Jetpack Compose (MVVM) client for the Personal Finance system. It **reuses**
`expense-core` for every business rule, computation and validation.

## How it consumes the shared core
- ViewModels depend on `repository.FinanceRepository` (a narrow, UI-shaped port).
- `repository.CoreFinanceRepository` implements that port by delegating to the
  core `ExpenseManager` services — no rules are duplicated on the client.
- `data.CoreProvider` builds the `ExpenseManager` by opening the core's
  JDBC/SQLite database against a file in the app's private storage, and
  `MainActivity` wires `CoreProvider → CoreFinanceRepository → ViewModels →
  navigation` (with a small first-run bootstrap that seeds a default account and
  categories).
- **On-device data-layer caveat:** the desktop-oriented `sqlite-jdbc` artifact
  bundles native binaries for desktop OSes, not Android ABIs. Running on a
  physical device therefore requires either an Android-compatible SQLite-JDBC
  build or the native-SQLite adapters that implement the core `*Repository`
  ports directly (iteration 2). Either way the same `ExpenseService` /
  `MonthlySummaryService` run unchanged — only the persistence adapter differs.

## Screens & wiring
- `MainActivity` (single-activity Compose host) builds the object graph and hosts
  the navigation graph.
- Dashboard (implemented), Quick Expense (implemented).
- Quick Income, History, Reports, Settings follow the same MVVM pattern and are
  added in iteration 2.
