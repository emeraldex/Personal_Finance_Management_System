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
- A bottom navigation bar wires six destinations, all implemented:
  - **Dashboard** — headline cards + category breakdown, with a month pager.
  - **Add Expense** / **Add Income** — shared `EntryForm` with account and
    category pickers, backed by `QuickEntryViewModel`.
  - **History** — the month's transactions with per-row delete (`HistoryViewModel`).
  - **Reports** — payment-method, budget-utilisation and cash-flow breakdowns
    plus a set-budget form (`ReportsViewModel`).
  - **Settings** — read-only environment/about info (currency, data location).
- Each screen follows the same MVVM shape (`ViewModel` exposing a `StateFlow`,
  a stateless Composable). The default currency is **MYR**, matching desktop.
- The narrow `FinanceRepository` port now also exposes accounts, categories,
  payment methods, month transactions, delete and set-budget — every one
  delegating to the shared core; no rules are duplicated on the client.

## Building
This module is a standard Gradle/Android project and needs the Android SDK plus
Gradle (or Android Studio) — it is **not** part of the Maven reactor. Build the
core jar it consumes first, then assemble the app:

```bash
# 1. from expense-manager/: publish the core jar the app references
mvn -pl expense-core -am install
# 2. from expense-android/: build the debug APK (needs local.properties -> sdk.dir,
#    or ANDROID_HOME set, and a Gradle wrapper / installed Gradle)
gradle :app:assembleDebug        # or: ./gradlew :app:assembleDebug
```

- **R8 / minify note:** the consumed `expense-core-1.0.0.jar` also contains the
  desktop-only `PoiWorkbookExporter` / `PdfSummaryExporter`, which reference
  Apache POI / PDFBox. Those classes are never loaded on Android, so ordinary
  (non-minified) debug/release builds only emit D8 "missing class" warnings. If
  you enable minification, add to `proguard-rules.pro`:

  ```
  -dontwarn org.apache.poi.**
  -dontwarn org.apache.pdfbox.**
  -dontwarn org.apache.xmlbeans.**
  ```
- The on-device `sqlite-jdbc` caveat above still applies: the app compiles and
  packages, but running the JDBC-backed core on a physical device/emulator needs
  an Android-compatible SQLite adapter (iteration 2).
