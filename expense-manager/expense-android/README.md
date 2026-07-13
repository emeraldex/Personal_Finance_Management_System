# expense-android

Jetpack Compose (MVVM) client for the Personal Finance system. It **reuses**
`expense-core` for every business rule, computation and validation.

## How it consumes the shared core
- ViewModels depend on `repository.FinanceRepository` (a narrow, UI-shaped port).
- `repository.CoreFinanceRepository` implements that port by delegating to the
  core `ExpenseManager` services — no rules are duplicated on the client.
- `data.CoreProvider` builds the `ExpenseManager` from **`android.database.sqlite`
  adapters** (`data.Android*Repository`, implementing the core repository ports),
  opening a database file in the app's private storage. `MainActivity` wires
  `CoreProvider → CoreFinanceRepository → ViewModels → navigation` (with a small
  first-run bootstrap that seeds a default account and categories).
- **On-device persistence (resolved):** the app uses no JDBC driver. The seven
  `Android*Repository` adapters run the exact same SQL and share the same
  `db/schema.sql` (loaded from the core jar) as the desktop JDBC build, and
  `ExpenseManager`'s dependency-injection constructor accepts them — so the same
  `ExpenseService` / `MonthlySummaryService` / analytics run on device, only the
  storage adapter differs. Upserts avoid `ON CONFLICT`/`RETURNING` (a read-then-
  write instead) to support older Android SQLite. The adapters are verified by a
  Robolectric test (`app/src/test/.../AndroidPersistenceTest`) that exercises the
  full stack against real Android SQLite on the JVM — no emulator needed.

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
# 2. from expense-android/: build the debug APK and run the SQLite adapter tests
#    (needs local.properties -> sdk.dir, or ANDROID_HOME set)
./gradlew :app:assembleDebug :app:testDebugUnitTest
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
- On-device persistence is done: the app uses `android.database.sqlite` adapters
  (no JDBC driver), so it runs on a real device/emulator. See "On-device
  persistence (resolved)" above.
